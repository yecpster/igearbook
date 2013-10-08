package net.jforum.view.admin.upgrade;

import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.entities.Group;
import net.jforum.security.PermissionControl;
import net.jforum.security.Role;
import net.jforum.security.RoleCollection;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;

public abstract class GenericUpgradeService implements UpgradeService {
    protected void handleEntitlement(String permition, int moduleId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group enGroup = groupDao.getEntitlementGroup(permition, moduleId);
        if (enGroup == null) {
            enGroup = groupDao.addNewEntitlementGroup(permition, moduleId);
        }
        RoleCollection roles = gmodel.loadRoles(enGroup.getId());
        if (roles == null || roles.size() == 0) {
            this.addRole(pc, permition, moduleId, enGroup);
        }
    }

    protected void addRole(PermissionControl pc, String roleName, int id, Group group) {
        Role role = new Role();
        role.setName(roleName);

        RoleValueCollection roleValues = new RoleValueCollection();

        RoleValue rv = new RoleValue();
        rv.setValue(Integer.toString(id));
        roleValues.add(rv);

        pc.addRoleValue(group.getId(), role, roleValues);
    }

    protected void addRole(PermissionControl pc, String roleName, Group group) {
        Role role = new Role();
        role.setName(roleName);

        pc.addRole(group.getId(), role);
    }
}
