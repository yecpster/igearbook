package net.jforum.view.admin;

import net.jforum.util.preferences.TemplateKeys;
import net.jforum.view.admin.upgrade.Upgrade2_2_0;
import net.jforum.view.admin.upgrade.UpgradeService;

/**
 * 
 * @author Chesley
 * 
 */
public class UpgradeAction extends AdminCommand {
    // Listing
    public void list() {
        upgrade();
        this.setTemplateName(TemplateKeys.ADMIN_UPGRADE);
    }

    public void upgrade() {
        UpgradeService upgradeService = new Upgrade2_2_0();
        upgradeService.upgrade();
    }

}
