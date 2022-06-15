package lensrandoms.wurm.lensrandoms;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;

public class FunnyAction implements ModAction, BehaviourProvider, ActionPerformer {

	private static Logger logger = Logger.getLogger(FunnyAction.class.getName());

	private final short actionId;
	private final ActionEntry ae;
		
	
	public FunnyAction() {
		actionId = (short) ModActions.getNextActionId();
		ae = new ActionEntryBuilder(actionId, "Fart", "farting", new int[] {6,48,37}).build();
		ModActions.registerAction(ae);
	}
	@Override
	public BehaviourProvider getBehaviourProvider() {
		return this;
	}

	@Override
	public ActionPerformer getActionPerformer() {
		return this;
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Creature target) {
		if (performer instanceof Player) {
			return Arrays.asList(ae);
		} else {
			return null;
		}
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Creature target) {
		return getBehavioursFor(performer, null, target);
	}

	@Override
	public short getActionId() {
		return actionId;
	}

	@Override
	public boolean action(Action action, Creature performer, Creature target, short num, float counter) {
		return action(action, performer, null, target, num, counter);
	}

	@Override
	public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
		try {
			if (counter == 1.0f) {
			performer.getCommunicator().sendNormalServerMessage("You clench your bowels...");
			
			final int time = 25;
			performer.getCurrentAction().setTimeLeft(time);
			performer.sendActionControl("Farting", true, time);
			} else {
				int time = performer.getCurrentAction().getTimeLeft();
				if (counter*10.0f > time) {
					performer.getCommunicator().sendNormalServerMessage("You squeeze out a fart. Disgusting.");
					return true;
				}
			}
		return false;
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			return true;

		}
	}
}
