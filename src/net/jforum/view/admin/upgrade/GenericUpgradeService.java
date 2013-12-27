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

import org.apache.commons.lang3.StringUtils;

public abstract class GenericUpgradeService implements UpgradeService {
    protected Group createEntitlementGroupIfAbsent(final String permission, final int moduleId) {
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group enGroup = groupDao.getEntitlementGroup(permission, moduleId);
        if (enGroup == null) {
            enGroup = groupDao.addNewEntitlementGroup(permission, moduleId);
        }
        final RoleCollection roles = gmodel.loadRoles(enGroup.getId());
        if (roles == null || roles.size() == 0) {
            this.addRole(pc, permission, moduleId, enGroup);
        }
        return enGroup;
    }

    protected void addRole(final PermissionControl pc, final String roleName, final int id, final Group group) {
        final Role role = new Role();
        role.setName(roleName);

        final RoleValueCollection roleValues = new RoleValueCollection();

        final RoleValue rv = new RoleValue();
        rv.setValue(Integer.toString(id));
        roleValues.add(rv);

        pc.addRoleValue(group.getId(), role, roleValues);
    }

    protected void addRole(final PermissionControl pc, final String roleName, final Group group) {
        final Role role = new Role();
        role.setName(roleName);

        pc.addRole(group.getId(), role);
    }

    protected void executeUpdateSql(final String sqlFileName) {
        final Connection conn = JForumExecutionContext.getConnection();
        @SuppressWarnings("unchecked")
        final
        List<String> statements = ParseDBStructFile.parse(SystemGlobals.getValue(ConfigKeys.CONFIG_DIR) + "/database/mysql/" + sqlFileName);
        for (final String query : statements) {
            if (StringUtils.isBlank(query)) {
                continue;
            }

            Statement s = null;
            try {
                s = conn.createStatement();
                s.executeUpdate(query);
            } catch (final SQLException ex) {
                throw new DatabaseException(ex);
            } finally {
                DbUtils.close(s);
            }
        }
    }
}
