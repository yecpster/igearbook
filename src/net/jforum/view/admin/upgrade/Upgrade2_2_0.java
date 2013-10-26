package net.jforum.view.admin.upgrade;

import net.jforum.dao.CategoryDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.dao.UserDAO;
import net.jforum.entities.Category;
import net.jforum.entities.Group;
import net.jforum.repository.ForumRepository;
import net.jforum.repository.RolesRepository;
import net.jforum.repository.SecurityRepository;
import net.jforum.security.PermissionControl;
import net.jforum.security.RoleCollection;
import net.jforum.security.SecurityConstants;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

public class Upgrade2_2_0 extends GenericUpgradeService implements UpgradeService {
    private static final String DEFAULT_TEAM_CATEGORY = "default team category";
    private static final String SQL_FILE_NAME = "Upgrade2.2.0.sql";

    @Override
    public void upgrade() {
//        createTeamCategory();
        createEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_CREATE);
        createEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_ADMIN);

        SecurityRepository.clean();
        RolesRepository.clear();
    }

    private void createEntitlementGroup(String permition) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group enGroup = groupDao.selectByName(permition);
        if (enGroup == null) {
            enGroup = groupDao.addNewEntitlementGroup(permition);
        }
        RoleCollection roles = gmodel.loadRoles(enGroup.getId());
        if (roles == null || roles.size() == 0) {
            this.addRole(pc, permition, enGroup);
        }
    }

    private void createTeamCategory() {
        CategoryDAO cateDao = DataAccessDriver.getInstance().newCategoryDAO();
        Category category = new Category();
        category.setName(DEFAULT_TEAM_CATEGORY);
        category.setModerated(false);
        category.setType(1);
        cateDao.addNew(category);
        ForumRepository.addCategory(category);

        Group accessGroup = createEntitlementGroupIfAbsent(SecurityConstants.PERM_CATEGORY, category.getId());
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group defaultGroup = groupDao.selectById(SystemGlobals.getIntValue(ConfigKeys.DEFAULT_USER_GROUP));
        groupDao.updateChildGroups(accessGroup.getId(), new int[] { defaultGroup.getId() });
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();
        int anonymousUserId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
        userDao.addToGroup(anonymousUserId, new int[] { accessGroup.getId() });
    }

}
