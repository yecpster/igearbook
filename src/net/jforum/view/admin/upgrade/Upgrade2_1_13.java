package net.jforum.view.admin.upgrade;

import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.entities.Group;
import net.jforum.security.PermissionControl;
import net.jforum.security.RoleCollection;
import net.jforum.security.SecurityConstants;

public class Upgrade2_1_13 extends GenericUpgradeService implements UpgradeService {

    @Override
    public void upgrade() {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        String permition = SecurityConstants.PERM_TEAMFORUM_CREATE;
        Group enGroup = groupDao.selectByName(permition);
        if (enGroup == null) {
            enGroup = groupDao.addNewEntitlementGroup(permition);
        }
        RoleCollection roles = gmodel.loadRoles(enGroup.getId());
        if (roles == null || roles.size() == 0) {
            this.addRole(pc, permition, enGroup);
        }

    }

}
