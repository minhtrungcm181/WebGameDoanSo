package cybersoft.javabackend.java18.gamedoanso.servlet;

import cybersoft.javabackend.java18.gamedoanso.model.GameSession;
import cybersoft.javabackend.java18.gamedoanso.repository.GameSessionRepository;
import cybersoft.javabackend.java18.gamedoanso.repository.PlayerRepository;
import cybersoft.javabackend.java18.gamedoanso.service.GameService;
import cybersoft.javabackend.java18.gamedoanso.utils.JspUtils;
import cybersoft.javabackend.java18.gamedoanso.utils.UrlUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@WebServlet(name = "Ranking", urlPatterns = {
        UrlUtils.XEP_HANG
}
)
public class RankingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case UrlUtils.XEP_HANG -> processRanking(req,resp);
            default -> resp.sendRedirect(req.getContextPath() + UrlUtils.NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRanking(req,resp);
    }
    private void processRanking (HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        var ranking = new GameSessionRepository().rankingByDuration();
        req.setAttribute("rankings", ranking);
        req.getRequestDispatcher(JspUtils.XEP_HANG)
                .forward(req, resp);
    }
}
