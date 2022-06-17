package lensrandoms.wurm.lensrandoms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.CodeReplacer;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.LocalNameLookup;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.items.Item;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

public class LensRandoms implements WurmServerMod, Configurable, Initable, PreInitable, ServerStartedListener  {

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	private static final Random random = new Random();
	
	private boolean noDecayOnDeeds= true;
	private boolean enableBonsaiTrees= true;
	private boolean bonsaiModels= true;
	private boolean moarPlantables= false;
	
	ClassPool cPool = HookManager.getInstance().getClassPool();
	
	
	//Plant stuff
	public static List<Integer> plantables = new ArrayList<Integer>(Arrays.asList(6,409,410,412,801,1283));
	private int[] bonusPlant = {134,246,247,248,249,250,251,364,367,411,414,832,833,1184,1196,1280,1235};
	
    static final String[] models = new String[] {
            "basil", "belladonna", "cumin", "fennelplant", "ginger", "lovage", "mint",
            "oregano", "paprika", "parsley", "rosemary", "sage", "thyme", "turmeric" };
	
    
    //Do decay code
	public static boolean doDecayCode(boolean decay,boolean onDeed,boolean decaytimeql,boolean isBulk,int isRandTick) {
		return (decay && !onDeed && (decaytimeql || isBulk || isRandTick == 0));
	}
	/* Future Features 
	 *  	Rename animals?
	 * 		Level up- like level but no needed flat
	 * 
	 * 
	 * 
	 * 
	 */	
	
	@Override
	public void configure(Properties properties) {
		noDecayOnDeeds = Boolean.valueOf(properties.getProperty("noDecayOnDeeds", Boolean.toString(noDecayOnDeeds)));
		enableBonsaiTrees = Boolean.valueOf(properties.getProperty("enableBonsaiTrees",Boolean.toString(enableBonsaiTrees)));
		bonsaiModels = Boolean.valueOf(properties.getProperty("bonsaiModels",Boolean.toString(bonsaiModels)));
		moarPlantables = Boolean.valueOf(properties.getProperty("moarPlantables",Boolean.toString(moarPlantables)));
		
		logger.log(Level.INFO,"Items do not decay while on a deed: "+ noDecayOnDeeds);
		logger.log(Level.INFO,"You can make Bonsai Trees: "+ enableBonsaiTrees);
		logger.log(Level.INFO,"Do custom pots have models?: " + bonsaiModels);
		logger.log(Level.INFO,"You can plant more than just fruit: "+ moarPlantables);
	}
	
	@Override
	public void preInit() {
		if (enableBonsaiTrees) {
			
			if (moarPlantables) {
				for (int i=0;i<bonusPlant.length;i++) {
					plantables.add(bonusPlant[i]);
				}
			}
		
		
        if (!bonsaiModels)
            return;
        	try {
            	/**
             	* Effectively overrides the model name for the filled planter,
             	* otherwise the models will be question mark bags instead,
             	* because "model.planter.pickaxe.*" does not exist for example.
             	* 
             	* Modified Shamelessly from dmon82's github.
             	*/
            	CtClass item = cPool.get("com.wurmonline.server.items.Item");
            	CtMethod planterMethod = item.getMethod("getModelName", "()Ljava/lang/String;");
            
            
            	StringBuilder sb = new StringBuilder();
            	sb.append("{");
            	sb.append("if (getTemplateId() == 1162) {");
            
            	for (int templateId : plantables) {
            		random.setSeed(templateId);
                	String modelName = models[random.nextInt(models.length)];
                
                	sb.append("    if (getRealTemplateId() == ").append(templateId).append(") return \"model.planter.");
                	sb.append(modelName).append("\";");
                
                	logger.info(String.format("Using model '%s' for template Id %d.", modelName, templateId));
            	}
            
            	sb.append("}");
            	sb.append("}");
            
            	planterMethod.insertBefore(sb.toString());
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE, "Can't make the pots look pretty :C", e);
        	}
		}
		ModActions.init();
	}
	
	public static Boolean isPlantable(Item subject) {
		for (int i : plantables) {
			if (subject.getTemplateId() == i) {
				return true;
			}
		}
		return false;
	}
	@Override
	public void init() { 
		
		
		/*if (variable) {
			try {
				CtClass[] paramTypes = {
						CtPrimitiveType.longType,
						HookManager.getInstance().getClassPool().get("com.wurmonline.items.DbStrings")
				};
				
				HookManager.getInstance().registerHook("com.wurmonline.server.items", "decay", Descriptor.ofMethod(CtPrimitiveType.voidType, paramTypes), new InvocationHandlerFactory() {
					
					@Override
					public InvocationHandler createInvocationHandler() {
						return new InvocationHandler() {
							
							@Override
							public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
								
								return null;
							}
						};
					}
				});
				
				
			} catch (NotFoundException e) {
  				throw new HookException(e);
  			}
		} */

		if (noDecayOnDeeds) {
			CtClass ctItem = null;
			try {
			 ctItem = cPool.get("com.wurmonline.server.items.Item");
			
			} catch (NotFoundException e) {
				logger.log(Level.SEVERE,"I cant find the Item class!");
				throw new RuntimeException(e);
			}
			
			
			CtClass[] paramTypes = {
					ctItem,
					CtPrimitiveType.intType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType,
					CtPrimitiveType.booleanType
			};
			
			CtMethod method = null;
			try {
				method = ctItem.getMethod("poll", Descriptor.ofMethod(CtPrimitiveType.booleanType, paramTypes));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find the Poll method!");
				throw new RuntimeException(e1);
			}
			
			MethodInfo methodInfo = method.getMethodInfo();
			CodeAttribute codeAttribute= methodInfo.getCodeAttribute();

			
			LocalNameLookup localNames = new LocalNameLookup((LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag));

			
			//Now for the Bytecode we're looking for...
			//Line 20626 in com.wurmonline.server.items.Item L121
			//Bless you 'wyvernmods' for bytecode tools. Bless.
			
			BytecodeTools bytecode = new BytecodeTools(methodInfo.getConstPool());
			
			//This took 3 bloody days to get right.
			//THREE DAYS. OF NONSTOP. CODING.
			
			try {
				bytecode.addIload(localNames.get("decay"));
			} catch (NotFoundException e2) {
				logger.log(Level.SEVERE, "Can't find decay!");
				throw new RuntimeException(e2);
			}
			bytecode.add(bytecode.IFEQ);
			bytecode.addIndex(304);
			
			try {
				bytecode.addIload(localNames.get("decaytimeql"));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find decaytimeql!");
				throw new RuntimeException(e1);
			}
			bytecode.add(bytecode.IFNE);
			bytecode.addIndex(21);
			bytecode.addAload(0);
			bytecode.addMethodIndex(bytecode.INVOKEVIRTUAL, "isBulkItem", "()Z", "com.wurmonline.server.items.Item");
			bytecode.add(bytecode.IFNE);
			bytecode.addIndex(14);
			bytecode.addFieldIndex(bytecode.GETSTATIC,"rand","Ljava/util/Random;", "com.wurmonline.server.Server");
			try {
				bytecode.addIload(localNames.get("num"));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE,"Cant find num!");
				throw new RuntimeException(e1);
			}
			bytecode.addMethodIndex(bytecode.INVOKEVIRTUAL,"nextInt","(I)I", "java.util.Random");
			bytecode.add(bytecode.IFNE);
			bytecode.addIndex(281);
			byte[] search = bytecode.get();
			
			
			//Bytecode to replace it
			bytecode = new BytecodeTools(methodInfo.getConstPool());
			bytecode.addFieldIndex(bytecode.GETSTATIC,"rand","Ljava/util/Random;", "com.wurmonline.server.Server");
			try {
				bytecode.addIload(localNames.get("num"));
			} catch (NotFoundException e1) {
				logger.log(Level.SEVERE, "Cant find num!");
				throw new RuntimeException(e1);
			}
			bytecode.addMethodIndex(bytecode.INVOKEVIRTUAL,"nextInt","(I)I", "java.util.Random");
			bytecode.addAload(0);
			bytecode.addMethodIndex(bytecode.INVOKEVIRTUAL, "isBulkItem", "()Z", "com.wurmonline.server.items.Item");
			
			try {
			bytecode.addIload(localNames.get("decaytimeql"));
			bytecode.addIload(localNames.get("deeded"));
			bytecode.addIload(localNames.get("decay"));
			} catch (NotFoundException e) {
				logger.log(Level.SEVERE, "Cant find local name for decay,deeded, or decaytimeql!");
				throw new RuntimeException(e);
			}
			bytecode.addMethodIndex(Opcode.INVOKESTATIC,"doDecayCode",Descriptor.ofMethod
					(CtPrimitiveType.booleanType,
					new CtClass[] {CtPrimitiveType.booleanType,CtPrimitiveType.booleanType,CtPrimitiveType.booleanType,CtPrimitiveType.booleanType,CtPrimitiveType.intType}),
					this.getClass().getName());
			bytecode.addGap(search.length - bytecode.length() - 3);
			bytecode.add(bytecode.IFNE);
			bytecode.addIndex(281);
			byte[] replacement = bytecode.get();
			
			//Actually replace code
			try {
				new CodeReplacer(codeAttribute).replaceCode(search, replacement);
				methodInfo.rebuildStackMap(cPool); // This part is very important
			} catch (NotFoundException | BadBytecode e) {
				logger.log(Level.SEVERE, "Either the bytecode is bad, or it cant find the bytecode to replace! " + e);
				throw new RuntimeException(e);
			}
			
			
		}
	}

	@Override
	public void onServerStarted() {
		logger.log(Level.INFO,"LensRandoms actions registering...");
		if (enableBonsaiTrees) {
			ModActions.registerAction(new PlantBonsaiAction());
			ModActions.registerAction(new PickBonsaiAction());
			logger.log(Level.INFO, "Bonsai trees enabled!");
		}
	}


	
}
