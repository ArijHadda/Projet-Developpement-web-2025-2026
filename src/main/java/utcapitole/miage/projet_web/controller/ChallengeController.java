package utcapitole.miage.projet_web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.Challenge;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ChallengeService;
import utcapitole.miage.projet_web.model.jpa.SportRepository;

@Controller
@RequestMapping("/challenge")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private SportRepository sportRepository;

    @GetMapping("/{id}/classement")
    public String showClassement(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        // 2. 获取排好序的 DTO 列表传给前端
        model.addAttribute("classementList", challengeService.getClassement(challengeId));

        // 3. 返回对应的 Thymeleaf 模板名称
        return "challenge-classement";
    }

    // 1. 展示所有挑战的列表 (大厅)
    @GetMapping("/list")
    public String listerChallenges(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";

        model.addAttribute("challenges", challengeService.getAllChallenges());
        return "challenge-list"; // 对应 src/main/resources/templates/challenge-list.html
    }

    // 2. 显示创建挑战的表单页面
    @GetMapping("/create")
    public String showCreateForm(Model model, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";

        model.addAttribute("challenge", new Challenge());
        // 👇 把数据库里所有的运动传给前端，供下拉菜单使用
        model.addAttribute("sports", sportRepository.findAll());

        return "challenge-create";
    }

    // 3. 处理前端提交的创建请求
    @PostMapping("/create")
    public String createChallenge(@ModelAttribute("challenge") Challenge challenge, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        challengeService.creerChallenge(challenge, user);
        return "redirect:/challenge/list"; // 创建成功后跳回列表页
    }

    // 4. 处理用户加入挑战的请求
    @PostMapping("/{id}/join")
    public String rejoindreChallenge(@PathVariable("id") Long challengeId, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        try {
            challengeService.rejoindreChallenge(challengeId, user);
        } catch (RuntimeException e) {
            // 这里可以处理重复加入的异常，比如给页面传一个 error 信息
            System.out.println(e.getMessage());
        }

        return "redirect:/challenge/" + challengeId + "/classement"; // 加入后直接跳转到该挑战的排行榜
    }

    // 5. 处理删除挑战的请求
    @PostMapping("/{id}/delete")
    public String supprimerChallenge(@PathVariable("id") Long challengeId, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        try {
            challengeService.supprimerChallenge(challengeId, user);
        } catch (RuntimeException e) {
            System.out.println("Erreur suppression: " + e.getMessage());
        }
        return "redirect:/challenge/list"; // 删除后跳回列表页
    }

    // 6. 显示修改挑战的页面
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long challengeId, Model model, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        // 找出这个挑战，并传给前端
        Challenge challenge = challengeService.getAllChallenges().stream()
                .filter(c -> c.getId().equals(challengeId))
                .findFirst().orElse(null);

        // 安全拦截：如果挑战不存在，或者当前用户不是创建者，直接踢回列表页
        if (challenge == null || !challenge.getCreateur().getId().equals(user.getId())) {
            return "redirect:/challenge/list";
        }

        model.addAttribute("challenge", challenge);
        return "challenge-edit"; // 对应新建的 challenge-edit.html
    }

    // 7. 处理修改标题的提交
    @PostMapping("/{id}/edit")
    public String modifierChallenge(@PathVariable("id") Long challengeId, @RequestParam("titre") String nouveauTitre, jakarta.servlet.http.HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/user/login";

        try {
            challengeService.modifierTitreChallenge(challengeId, nouveauTitre, user);
        } catch (RuntimeException e) {
            System.out.println("Erreur modification: " + e.getMessage());
        }
        return "redirect:/challenge/list";
    }
}
