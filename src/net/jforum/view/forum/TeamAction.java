package net.jforum.view.forum;

import net.jforum.Command;
import net.jforum.util.preferences.TemplateKeys;

import org.apache.log4j.Logger;

/**
 * 
 * @author Chesley
 * 
 */
public class TeamAction extends Command {
    private static Logger LOGGER = Logger.getLogger(TeamAction.class);

    @Override
    public void list() {
        this.setTemplateName(TemplateKeys.TEAM_LIST);
    }

    public void insert() {
        this.setTemplateName(TemplateKeys.TEAM_INSERT);
    }
}