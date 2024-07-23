package br.com.marcos.projeto.thsaws.service;

import br.com.marcos.projeto.thsaws.dto.DtoPerfil;
import br.com.marcos.projeto.thsaws.model.ThsCadastro;
import br.com.marcos.projeto.thsaws.model.ThsEntrar;
import br.com.marcos.projeto.thsaws.repository.RepositoryCadastro;
import br.com.marcos.projeto.thsaws.repository.RepositoryEntrar;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Service
public class ServicePerfil {

    @Autowired
    private RepositoryCadastro repositoryCadastro;

    @Autowired
    private RepositoryEntrar repositoryEntrar;

    public ModelAndView paginaPerfil() {

        ModelAndView mv = new ModelAndView("perfil");
        DtoPerfil dtoPerfil = new DtoPerfil();
        mv.addObject("dtoPerfil", dtoPerfil);
        return mv;

    }

    public ModelAndView editarPost(@PathVariable Long id, @Valid DtoPerfil dtoPerfil, BindingResult bindingResult, HttpServletRequest request) {

        ModelAndView mv = new ModelAndView("perfil");

        ThsCadastro cadastro = dtoPerfil.requisicaoCadastro();

        var usuarioExiste = repositoryCadastro.findByUsuario(cadastro.getUsuario());

        mv.addObject("dtoPerfil", dtoPerfil);

        if (bindingResult.hasErrors()) {
            return mv;
        }
        if (usuarioExiste.isPresent()) {
            System.out.println("Entrou no is empty");

            //ThsCadastro thsCadastro = usuarioExiste.get();


            repositoryCadastro.save(cadastro);

            request.setAttribute("usuario", cadastro.getUsuario()); // o nome do usuario foi armazenado na sessão após um cadastro bem sucedido
            request.setAttribute("id", cadastro.getId());

            return new ModelAndView("redirect:/");

        } else {
            bindingResult.rejectValue("usuario", "error.dtoPerfil", "O Usuario ja existe");
            return new ModelAndView("perfil");
        }
    }

    public ModelAndView editarGet(@PathVariable Long id, DtoPerfil dtoPerfil, HttpServletRequest request) {

        ModelAndView mv = new ModelAndView("perfil");

        var optionalCadastro = repositoryCadastro.findById(id);

        Long usuarioLogadoId = (Long) request.getSession().getAttribute("id");

        if (usuarioLogadoId == null) {
            System.err.println("Usuário não está logado. Redirecionando para a página inicial.");
            return new ModelAndView("redirect:/");
        }

        if (optionalCadastro.isPresent()) {

            ThsCadastro cadastro = optionalCadastro.get();

            dtoPerfil.fromDtoCadastro(cadastro);

            mv.addObject("dtoPerfil", dtoPerfil);

            mv.addObject("usuarioLogadoId", usuarioLogadoId);

        } else {
            System.err.println("Id não encontrado no banco");
        }

        return mv;
    }

    public ModelAndView sairDaConta(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("perfil");

        var verificandoUsuario = repositoryCadastro.findById(id);

        if (verificandoUsuario.isPresent()) {

            Long usuarioLogadoId = (Long) request.getSession().getAttribute("id");

            mv.addObject("usuarioLogadoId", usuarioLogadoId);

            repositoryCadastro.deleteById(verificandoUsuario.get().getId());

            HttpSession session = request.getSession(false); // Obtendo a Sessão Atual

            if (session != null) {
                session.invalidate(); // Invalidando a Sessão
            }

            Cookie[] cookies = request.getCookies(); // Obtém Todos os Cookies da Requisição

            if (cookies != null) { // Verifica se Existe Algum Cookie
                for (Cookie cookie : cookies) {
                    if ("usuario".equals(cookie.getName()) || "id".equals(cookie.getName())) { // Verifica se o Nome do Cookie é "usuario" ou "id"
                        cookie.setMaxAge(0); // indica que ele deve ser removido, por causa do valor 0
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
            }

            return new ModelAndView("redirect:/");

        } else {
            return mv;

        }
    }
}