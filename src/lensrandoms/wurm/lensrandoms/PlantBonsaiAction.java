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
import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class PlantBonsaiAction implements ModAction, BehaviourProvider, ActionPerformer {
	private static Logger logger = Logger.getLogger(PlantBonsaiAction.class.getName());

	private final short actionId;
	private final ActionEntry ae;
	
	public PlantBonsaiAction() {
		actionId = (short) ModActions.getNextActionId();
		ae = new ActionEntryBuilder(actionId, "Plant Bonsai", "Planting Bonsai", new int[] {48}).build();
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
	public List<ActionEntry> getBehavioursFor(Creature performer,Item subject,Item target){
		if (performer instanceof Player && subject.getTemplate().isFruit() && target.getTemplateId() == 1161) {
			return Arrays.asList(ae);
		} else {
			return null;
		}
	}
	
	@Override
	public boolean action(Action action,Creature performer, Item seed, Item pot, short num, float counter) {
		if (pot.getQualityLevel() < 50.0F) {
			performer.getCommunicator().sendNormalServerMessage("The pot doesn't look sturdy enough to hold the Bonsai that would grow.");
			return true;
		}
		if (!Methods.isActionAllowed(performer, action.getNumber())) {
		return true; 
		}
			if (counter == 1.0f) {
				try {
			performer.getCommunicator().sendNormalServerMessage("You pick a seed from the " + seed.getName() + " and plant it...");
			
			final int time = 5;
			performer.getCurrentAction().setTimeLeft(time);
			performer.sendActionControl("Planting Bonsai", true, time);
			return false;
				} catch (Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
					return true;
				} 
			} else {
				int time = action.getTimeLeft();
				if (counter*10.0f > time) {
					try {
						Item newPot = ItemFactory.createItem(1162, pot.getQualityLevel(), pot.getRarity(),performer.getName());
						newPot.setRealTemplate(seed.getTemplate().getGrows());
						newPot.setLastOwnerId(pot.getLastOwnerId());
						newPot.setDamage(pot.getDamage());
						Item parent = pot.getParentOrNull();
						if (parent != null && parent.getTemplateId() == 1110 && (parent.getItemsAsArray()).length > 30) {
				              performer.getCommunicator().sendNormalServerMessage("The pot will not fit back into the rack, so you place it on the ground.", (byte)2);
				              newPot.setPosXY(pot.getPosX(), pot.getPosY());
				              VolaTile tile = Zones.getTileOrNull(pot.getTileX(), pot.getTileY(), pot.isOnSurface());
				              if (tile != null)
				                tile.addItem(newPot, false, false); 
				            } else if (parent == null) {
				              newPot.setPosXYZRotation(pot.getPosX(), pot.getPosY(), pot.getPosZ(), pot.getRotation());
				              newPot.setIsPlanted(pot.isPlanted());
				              VolaTile tile = Zones.getTileOrNull(pot.getTileX(), pot.getTileY(), pot.isOnSurface());
				              if (tile != null)
				                tile.addItem(newPot, false, false); 
				            } else {
				              parent.insertItem(newPot, true);
				            } 
				         Items.destroyItem(pot.getWurmId());
				         performer.getCommunicator().sendNormalServerMessage("The seedling sprouts into a small " + seed.getName() + " tree!");
					} catch (NoSuchTemplateException nst) {
			            logger.log(Level.WARNING, nst.getMessage(), (Throwable)nst);
			          } catch (FailedException fe) {
			            logger.log(Level.WARNING, fe.getMessage(), (Throwable)fe);
			          } 
			        } else {
			          performer.getCommunicator().sendNormalServerMessage("Sadly, nothing sprouts.", (byte)3);
			        } 
		          Items.destroyItem(seed.getWurmId());
			      return true;
			    }
			  }
		}
