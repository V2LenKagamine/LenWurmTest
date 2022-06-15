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

import com.wurmonline.server.FailedException;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Herb;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;

public class PickBonsaiAction implements ModAction, BehaviourProvider, ActionPerformer	{
	
	private static Logger logger = Logger.getLogger(PickBonsaiAction.class.getName());

	private final short actionId;
	private final ActionEntry ae;
		
	
	public PickBonsaiAction() {
		actionId = (short) ModActions.getNextActionId();
		ae = new ActionEntryBuilder(actionId, "Groom Bonsai", "Grooming Bonsai", new int[] {48}).build();
		ModActions.registerAction(ae);
	}
	
	@Override
	public short getActionId() {
		return actionId;
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
	public List<ActionEntry> getBehavioursFor(Creature performer,Item held,Item target) {
		if (performer instanceof Player && target.getTemplateId() == 1162) {
			return Arrays.asList(ae);
		} else {
			return null;
		}
	}
	
	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer,Item target) {
		return getBehavioursFor(performer,null,target);
	}
	
	
	@Override
	public boolean action(Action action,Creature performer,Item target,short num,float counter) {
		return action(action,performer,null,target,num,counter);
	}
	
	@Override
	public boolean action(Action action,Creature performer,Item held,Item pot,short num,float counter) {
		int time = 0;
	    ItemTemplate growing = pot.getRealTemplate();
	    if (growing == null) {
	      performer.getCommunicator().sendNormalServerMessage("Not sure what is growing in here.", (byte)3);
	      return true;
	    } 
	    if (!Methods.isActionAllowed(performer, action.getNumber()))
	      return true; 
	    if (!canBePicked(pot)) {
	      performer.getCommunicator().sendNormalServerMessage("The tree is too small to groom.", (byte)3);
	      return true;
	    } 
	    if (!performer.getInventory().mayCreatureInsertItem()) {
	      performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put whatever you pick.");
	      return true;
	    }
	    Skill garden = performer.getSkills().getSkillOrLearn(10045);
	    if (counter == 1.0F) {
	    	time = 25;
	    	action.setTimeLeft(time);
	    	performer.getCommunicator().sendNormalServerMessage("You start to groom " + growing.getNameWithGenus() + " tree.");
	    	performer.sendActionControl("Grooming bonsai", true, time);
	    	return false;
	    }
	    time = action.getTimeLeft();
	    if (counter *10.0F > time) {
	    int knowledge = (int)garden.getKnowledge(0.0D);
	    double power = garden.skillCheck(0.0f, 0.0D, false, counter);
	    if (power < -50.0D) {
	    	performer.getCommunicator().sendNormalServerMessage("You nearly ruin the poor little tree!");
	    	pot.setAuxData((byte)(2));
	    	return true;
	    }
	    if (- 25.0D > power && power > -50.0D) {
	    	performer.getCommunicator().sendNormalServerMessage("You groom a little too much, but the tree seems to struggle on.");
	    	pot.setAuxData((byte)(4));
	    	return true;
	    }
	    try {
	    	float ql = Herb.getQL(power, knowledge);
	    	Item newFruit = ItemFactory.createItem(pot.getRealTemplateId(), Math.max(ql,  1.0f ), (byte)0, (byte)0, null);
	    	Item inventory = performer.getInventory();
	    	inventory.insertItem(newFruit);
	    	} catch (FailedException fe) {
	    		logger.log(Level.WARNING, performer.getName() + " " + fe.getMessage(), (Throwable)fe);
	    	} catch (NoSuchTemplateException nst) {
	            logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), (Throwable)nst);
	    	}
	    pot.setLastMaintained(WurmCalendar.currentTime);

	    if (power > -25.0D) {
	    	performer.getCommunicator().sendNormalServerMessage("Aww, there was a little fruit on the tree! You take it.");
	    	pot.setAuxData((byte)(0));
	    }
	    return true;
	    }
	   return false;
	}
	    

	private boolean canBePicked(Item pot) {
		if (pot.getTemplateId() != 1162) {
		return false;
		}
		ItemTemplate temp = pot.getRealTemplate();
		int age = pot.getAuxData() & Byte.MAX_VALUE;
		return (temp != null && age > 5);
	}
}
