  public class CropMod implements WurmMod, Configurable, Initable, PreInitable {
  	private boolean disableWeeds = true;
  	private Logger logger = Logger.getLogger(this.getClass().getName());

  	//
  	// The method configure is called when the mod is being loaded
  	// Configuration options shouldbe set here. Don't initialize the hooks
  	// here. It may break other mods
  	//
  	@Override
  	public void configure(Properties properties) {
  
  		disableWeeds = Boolean.valueOf(properties.getProperty("disableWeeds", Boolean.toString(disableWeeds)));
  		logger.log(Level.INFO, "disableWeeds: " + disableWeeds);
  	}

  	// 
  	// PreInit method
  	// This method should be used to change the class bytecode. The hooks may rename
  	// methods so referencing to a method by name later in the initialisation process
  	// may return a wrpper method
  	//
  	public void preInit() {
  		// Nothing to do here in this example
  	}

  	//
  	// Init method
  	// All the intialisation should happen here
  	//
  	public void init() {
  
  		//
  		// We initialize a method hook that gets called right before CropTilePoller.checkForFarmGrowth is called
  		//
  		if (disableWeeds) {
  			try {
  
  				//
  				// To make sure we hook the correct method the list of method parameter types is compiled
  				//
  				CtClass[] paramTypes = {
  						CtPrimitiveType.intType,
  						CtPrimitiveType.intType,
  						CtPrimitiveType.intType,
  						CtPrimitiveType.byteType,
  						CtPrimitiveType.byteType,
  						HookManager.getInstance().getClassPool().get("com.wurmonline.mesh.MeshIO"),
  						CtPrimitiveType.booleanType
  				};
  				
  				//
  				// next we register the hook for 
  				// com.wurmonline.server.zones.CropTilePoller.checkForFarmGrowth(int, int, int, byte, byte, MeshIO, boolean) 
  				//
  				HookManager.getInstance().registerHook("com.wurmonline.server.zones.CropTilePoller", "checkForFarmGrowth", Descriptor.ofMethod(CtPrimitiveType.voidType, paramTypes), new InvocationHandlerFactory() {
  
  					@Override
  					public InvocationHandler createInvocationHandler() {
  						return new InvocationHandler() {
  
  							//
  							// The actual hook is an InvocationHandler. It's invoke method is called instead of the hooked method.
  							// The object, method and arguments are passed as parameters to invoke()
  							//
  							@Override
  							public Object invoke(Object object, Method method, Object[] args) throws Throwable {
  								//
  								// When the hook is called we can do stuff depending on the input parameters
  								// Here we check if the tileAge is 6 (the second ripe stage)
  								//
  								byte aData = ((Number) args[4]).byteValue();
  								final int tileState = aData >> 4;
  								int tileAge = tileState & 0x7;
  								if (tileAge == 6) {
  									// tileAge is 6. Advancing it further would create weeds.
  									// Therefore we just exit here.
  									// return null is required if the hooked method has a void return type
  									return null;
  								}
  
  								//
  								// tileAge is not 6. We just continue by calling the hooked method
  								//
  								return method.invoke(object, args);
  							}
  						};
  					}
  				});
  			} catch (NotFoundException e) {
  				throw new HookException(e);
  			}
  		}
  	}
  }