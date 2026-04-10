package utcapitole.miage.projet_web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import utcapitole.miage.projet_web.model.Objectif;
import utcapitole.miage.projet_web.model.Utilisateur;
import utcapitole.miage.projet_web.model.jpa.ObjectifService;
import utcapitole.miage.projet_web.model.jpa.SportService;

import java.util.Optional;

@Controller
@RequestMapping("/objectif")
public class ObjectifController {

    @Autowired
    private ObjectifService objectifService;

    @Autowired
    private SportService sportService;

    // 1. 查看目标列表 (Read)
    @GetMapping("/list")
    public String listerObjectifs(Model model, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        model.addAttribute("objectifsProgress", objectifService.getObjectifsAvecProgression(currentUser));
        return "objectif-list";
    }

    // 2. 显示创建目标的表单 (Create - Show form)
    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/user/login";

        // 传一个空的目标对象给前端表单绑定
        model.addAttribute("objectif", new Objectif());
        // 把所有运动传过去，供用户在下拉菜单选择
        model.addAttribute("sports", sportService.getAll());
        return "objectif-form"; // 将去寻找 templates/objectif-form.html
    }

    // 3. 处理表单提交：新增或修改 (Create/Update - Process)
    @PostMapping("/save")
    public String saveObjectif(@ModelAttribute("objectif") Objectif objectif, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        // 强行把当前登录的用户绑定到这个目标上，防止恶意篡改
        objectif.setUtilisateur(currentUser);
        objectifService.enregistrerObjectif(objectif);

        // 保存成功后，重定向回列表页
        return "redirect:/objectif/list";
    }

    // 4. 显示修改目标的表单 (Update - Show form)
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        Optional<Objectif> objectifOpt = objectifService.getObjectifById(id);
        if (objectifOpt.isPresent()) {
            Objectif objectif = objectifOpt.get();

            // 【安全校验】只能修改自己的目标！
            if (!objectif.getUtilisateur().getId().equals(currentUser.getId())) {
                return "redirect:/objectif/list";
            }

            model.addAttribute("objectif", objectif);
            model.addAttribute("sports", sportService.getAll());
            return "objectif-form"; // 共用同一个表单页面
        }
        return "redirect:/objectif/list";
    }

    // 5. 删除目标 (Delete)
    @GetMapping("/{id}/delete")
    public String deleteObjectif(@PathVariable("id") Long id, HttpSession session) {
        Utilisateur currentUser = (Utilisateur) session.getAttribute("loggedInUser");
        if (currentUser == null) return "redirect:/user/login";

        Optional<Objectif> objectifOpt = objectifService.getObjectifById(id);
        if (objectifOpt.isPresent()) {
            Objectif objectif = objectifOpt.get();

            // 【安全校验】只能删除自己的目标！
            if (objectif.getUtilisateur().getId().equals(currentUser.getId())) {
                objectifService.supprimerObjectif(id);
            }
        }
        return "redirect:/objectif/list";
    }
}