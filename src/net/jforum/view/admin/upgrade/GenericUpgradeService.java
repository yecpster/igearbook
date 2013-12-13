package net.jforum.view.admin.upgrade;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.jforum.JForumExecutionContext;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.entities.Group;
import net.jforum.exceptions.DatabaseException;
import net.jforum.security.PermissionControl;
import net.jforum.security.Role;
import net.jforum.security.RoleCollection;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.util.DbUtils;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.view.install.ParseDBStructFile;

import org.apache.commons.lang.StringUtils;

public abstract class GenericUpgradeService implements UpgradeService {
    protected Group createEntitlementGroupIfAbsent(String permission, int moduleId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group enGroup = groupDao.getEntitlementGroup(permission, moduleId);
        if (enGroup == null) {
            enGroup = groupDao.addNewEntitlementGroup(permission, moduleId);
        }
        RoleCollection roles = gmodel.loadRoles(enGroup.getId());
        if (roles == null || roles.size() == 0) {
            this.addRole(pc, permission, moduleId, enGroup);
        }
        return enGroup;
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

    protected void executeUpdateSql(String sqlFileName) {
        Connection conn = JForumExecutionContext.getConnection();
        @SuppressWarnings("unchecked")
        List<String> statements = ParseDBStructFile.parse(SystemGlobals.getValue(ConfigKeys.CONFIG_DIR) + "/database/mysql/" + sqlFileName);
        for (String query : statements) {
            if (StringUtils.isBlank(query)) {
                continue;
            }

            Statement s = null;
            try {
                s = conn.createStatement();
                s.executeUpdate(query);
            } catch (SQLException ex) {
                throw new DatabaseException(ex);
            } finally {
                DbUtils.close(s);
            }
        }
    }
}
