import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.io.Console;

public class Configurations
{
	public static int MAP_INDEX = -1;
	public static Map<Integer, Integer> SWAPPED_IDS = new HashMap<Integer, Integer>();
	public static short[][] MAP_REGIONS;
	public static String DIRECTORY;

	public static final int 
		READ_377_MAPS = 1, READ_OSRS_MAPS = 2, READ_CUSTOM_MAPS = 4, READ_500_MAPS = 8,

		MAX_MAPS_317 = 1389, 
		MAX_MAPS_474 = 1883,
		MAX_OBJECTS_317 = 14003, 
		MAX_OBJECTS_474 = 27084;

	public static int LOCK_OBJECT_REVISION = -1;
	public static int READING = -1;

	public static void loadObjectDefinitions()
	{
		DIRECTORY = getWorkingDirectory();
		
		/*
		 * Load Map Regions ID for Coordinates. 
		 */
		ReadWriteBuffer buffer = ReadWriteBuffer.read(DIRECTORY.concat("/data/474MapRegions.DAT"));
		MAP_REGIONS = new short[buffer.readShort()][2];
		for (int i = 0; i < MAP_REGIONS.length; ++i)
		{
			MAP_REGIONS[i][0] = (short)buffer.readShort(); 
			MAP_REGIONS[i][1] = (short)buffer.readShort();
		}

		Arrays.sort(BLOCKED_OBJECTS);

		for (int[] swapping : SWAP_IDS)
			SWAPPED_IDS.put(swapping[0], swapping[1]);

		SWAP_IDS = null; //No longer needed.
	}

	public static ReadWriteBuffer getRevisionFile(String version)
	{
		if (version.equals("377"))
			return ReadWriteBuffer.read(DIRECTORY + "/data/MapData/377maps/");
		else if (version.equals("503"))
			return ReadWriteBuffer.read(DIRECTORY + "/data/MapData/503+Maps/");
		else if (version.equals("custom"))
			return ReadWriteBuffer.read(DIRECTORY + "/data/MapData/CustomMaps/");
		else if (version.equals("osrs"))
			return ReadWriteBuffer.read(DIRECTORY + "/data/MapData/OSRSMaps/");
		else {
			System.out.println("Error Loading Map Version -> " + version);
			return null;
		}
	}

	public static String getWorkingDirectory()
	{
		String path = (new java.io.File(".")).getAbsolutePath();
		return path.substring(0, path.length() - 5);
	}

	public static int getMapIdForLocation(int x, int y)
	{
		int mapX = x >> 6 << 6;
		int mapY = y >> 6 << 6;
		for (int i = 0; i < MAP_REGIONS.length; ++i)
		{
			if (MAP_REGIONS[i][0] == mapX && MAP_REGIONS[i][1] == mapY)
				return i;
		}
		return -1;
	}

	public static int locX(int x) 
	{
		return x - (x >> 6 << 6) + 32;
	}

	public static int locY(int y) 
	{
		return y - (y >> 6 << 6) + 16;
	}

	public static int realX(int x) 
	{
		return MAP_REGIONS[MAP_INDEX][0] + x;
	}

	public static int realY(int y) 
	{
		return MAP_REGIONS[MAP_INDEX][1] + y;
	}

	public static int getMapX()
	{
		return MAP_REGIONS[MAP_INDEX][0];
	}

	public static int getMapY()
	{
		return MAP_REGIONS[MAP_INDEX][1];
	}

	public static int getSwappedObjectID(int current_id)
	{
		Integer value = null;
		if ((value = SWAPPED_IDS.get(current_id)) != null)
		{
			if ((int)value <= getMaximumObjectID())
			{
				System.out.println("[SWAPPED ID]: ");
				return (int)value;
			}
		}
		return current_id;
	}

	public static void LOCK_OBJECT_REVISION(int revision)
	{
		LOCK_OBJECT_REVISION = revision;
	}
   
    /*
     * This allows you to remove unwanted objects for your revision as 
     * Use LOCK_OBJECT_REVISION(###) to set the Revision.
     * Add more revisions as needed.
     */
	public static int getMaximumObjectID()
	{
		if (LOCK_OBJECT_REVISION == 317)
			return 14003;
		if (LOCK_OBJECT_REVISION == 474)
			return 27084;
		return 65535;
	}

	public static int getReadingVersion()
	{
		if (READING == -1)
			return READING;
		return (READING + 3) * 100;
	}

	public static boolean isBlocked(int object_id)
	{
		if (!BLOCK_OBJECTS_ENABLED)
			return false;
		return Arrays.binarySearch(Configurations.BLOCKED_OBJECTS, object_id) > -1;
	}

	public static int getMapIndexFromPrompt(boolean tryagain)
	{
		Console c = System.console();
		String input = "";
		if (tryagain)
			input = c.readLine("Error Entering ID/Coordinates Please Try Again (X,Y): ");
		else
			input = c.readLine("Enter a Map Index ID or Give Coordinates (X,Y): ");
		String mapLoc[] = input.split(",");
		int mapIdx = -1;
		try 
		{
			if (mapLoc.length == 1) {
				mapIdx = Integer.parseInt(input);
			} else {
				int x = Integer.parseInt(mapLoc[0]);
				int y = Integer.parseInt(mapLoc[1]);
				mapIdx = getMapIdForLocation(x, y);
			}
		} catch (Exception e) { }
		if ((mapIdx & 1) != 1)
			return getMapIndexFromPrompt(true);
		if (mapIdx < 1 || mapIdx > MAP_REGIONS.length)
			return getMapIndexFromPrompt(true);
		return mapIdx == -1 ? getMapIndexFromPrompt(true) : mapIdx;	
	}

	/*
	 * You will rarely need to add additonal values to this, but these are Minimap Icons. Type = 22.
	 * When you need to add an icon to the map just use these values. These are stored for easier access.
	 * I.e. addObject(3200, 3200, 0, BANK, 22);
	 */

	public static final int MINIGAME = 738;
	public static final int GENERAL_STORE = 2733;
	public static final int SWORD_SHOP = 2734;
	public static final int RUNES_SHOP = 2735;
	public static final int BATTLE_AXE_SHOP = 2736;
	public static final int MED_HELM = 2737;
	public static final int BANK = 2738;
	public static final int QUEST = 2739;
	public static final int MINING = 2741;
	public static final int SMELTING = 2742;
	public static final int EXCLAMATION_POINT = 2745;
	public static final int HERB = 2753;
	public static final int WATER = 2771;
	public static final int FRYING_PAN = 2772;
	public static final int QUESTION_MARK = 7054;
	public static final int PORTAL = 15531;
	public static final int SHORTCUT = 17030;
	public static final int HUNTING = 19750;

	/*
	* While creating 474 revision maps these are all the objects which where not normally or no longer in the map!
	* This is used to basically remove unwanted objects out of a map automatically for ANY map loaded!
	*/
	private static final boolean BLOCK_OBJECTS_ENABLED = false;

	public static final int[] BLOCKED_OBJECTS =
	{
		30032, 36696, 28774, 36697, 36698, 31305, 28765, 31152, 31153, 31154, 37209, 35327, 35927, 
		35926, 28320, 35088, 31298, 28324, 28764, 31120, 31121, 28546, 28325, 28231, 38426, 38425, 
		35159, 31124, 29954, 31090, 28564, 28763, 37128, 37127, 35868, 35869, 37127, 37128, 38172, 
		29420, 28776, 35310, 30071, 30073, 30135, 30136, 30139, 31122, 31123, 33459, 29953, 31089, 
		31125, 31091, 31038, 31032, 31036, 31037, 33907, 37734, 32018, 31747, 31301, 31126, 28769, 
		28773, 29728, 30259, 30260, 37293, 31761, 31762, 33943, 35290, 37213, 37214, 37227, 37228, 
		37235, 37236, 37237, 37238, 37239, 37240, 37241, 37243, 37244, 32823, 32821, 31130, 33460, 
		33999, 35894, 35896, 35897, 37346, 37710, 28586, 28770, 33446, 33447, 35906, 35919, 35923, 
		35924, 35922, 29943, 29992, 33927, 33928, 35914, 35916, 35893, 35886, 35887, 29812, 29813, 
		29814, 34321, 34323, 34340, 34341, 34342, 34343, 34344, 34345, 34346, 34348, 34349, 34350, 
		30250, 28625, 36704, 35131, 35132, 37075, 37076, 28705, 28741, 37022, 27543, 32526, 32527, 
		32528, 32529, 31751, 34174, 35879, 35878, 35885, 31775, 31165, 31166, 37030, 28549, 28766, 
		28778, 29422, 28547, 35889, 35890, 30661, 30662, 30663, 30664, 29536, 31145, 31796, 30416, 
		30517, 30518, 34424, 30642, 30568, 30455, 30456, 30495, 30496, 30497, 30501, 30502, 35995, 
		30521, 30541, 30542, 30623, 35034, 35035, 35036, 35037, 35038, 35039, 35040, 35041, 35042, 
		35043, 35043, 35044, 35045, 35046, 35993, 35994, 28230, 28323, 28543, 28229, 28539, 28514, 
		28539, 28545, 32021, 33112, 33113, 35925, 28540, 28541, 31450, 28235, 30140, 35092, 35396, 
		36748, 36766, 36767, 29951, 38428, 38447, 38458, 28544, 31704, 29944, 37215, 37216, 29945, 
		37024, 35902, 35903, 36733, 31714, 35877, 37993, 37994, 4132
	};

	/*
	* Highly recommend NOT changing/removing these! Utilized for convertering 503+ and 317 map ids to the 474 revision.
	* THIS IS ONLY USED FOR WHEN TYPES MATCH! Keep in mind this will not change the types used!
	*/
	private static int[][] SWAP_IDS = 
	{ 
		{ 35301, 23807 },  { 35314, 23819 }, { 35311, 23819 }, { 468, 25107 },   { 542, 25110 },
		{ 35315, 23819 },  { 35726, 26889 }, { 31141, 26885 }, { 31144, 26891 }, { 35305, 23800 },
		{ 35725, 26883 },  { 35724, 26883 }, { 35723, 26883 }, { 35722, 26883 }, { 35721, 26872 },
		{ 35714, 26863 },  { 31092, 26883 }, { 31093, 26883 }, { 31748, 9541 },  { 37433, 10367 },
		{ 37434, 10368 },  { 37435, 10369 }, { 37436, 10370 }, { 1080, 18493 },  { 1079, 18493 },
		{ 37437, 15055 },  { 33791, 196 },   { 31704, 197 },   { 35067, 3948 },  { 35068, 3949 },
		{ 35069, 3950 },   { 35665, 21793 }, { 25730, 21792 }, { 35449, 21795 }, { 27912, 21799 },
		{ 27935, 1415 },   { 27934, 1415 },  { 35336, 23735 }, { 35330, 23735 }, { 35344, 23747 },
		{ 35340, 23743 },  { 35913, 23852 }, { 35339, 23735 }, { 35323, 23857 }, { 35334, 23735 },
		{ 35338, 23737 },  { 35337, 23736 }, { 35329, 23736 }, { 35341, 23744 }, { 35333, 23735 },
		{ 35332, 23735 },  { 35331, 23735 }, { 35322, 23829 }, { 35307, 23818 }, { 35343, 23745 },
		{ 35345, 23752 },  { 35236, 23650 }, { 35765, 18154 }, { 35764, 17464 }, { 35766, 24338 },
		{ 35755, 15542 },  { 35756, 24281 }, { 35235, 18158 }, { 35752, 24181 }, { 35325, 23877 },
		{ 35321, 23818 },  { 35309, 23818 }, { 35308, 23818 }, { 23781, 23818 }, { 38172, 24635 },
		{ 35753, 24200 },  { 35751, 24148 }, { 33473, 24177 }, { 33472, 24033 }, { 30796, 15552 },
		{ 35300, 23806 },  { 35754, 15541 }, { 33916, 5569 },  { 33918, 5570 },  { 35335, 23735 },
		{ 35299, 23805 },  { 35298, 23801 }, { 35316, 23821 }, { 35319, 23824 }, { 35317, 23823 },
		{ 35303, 23816 },  { 35304, 23817 }, { 31164, 23813 }, { 31207, 24282 }, { 31759, 2843 },
		{ 35313, 23819 },  { 35760, 24312 }, { 35758, 24311 }, { 35762, 24314 }, { 35763, 15544 },
		{ 35767, 24339 },  { 4132, 24263 },  { 31146, 23848 }, { 31147, 23848 }, { 31148, 23848 },
		{ 31149, 23848 },  { 31150, 23848 }, { 31151, 23848 }, { 35294, 23775 }, { 35295, 23797 },
		{ 35296, 23797 },  { 35324, 23876 }, { 313, 15506 },   { 5583, 15506 },  { 5584, 15507 },
		{ 30046, 4735 },   { 30047, 4735 },  { 30048, 4737 },  { 30049, 4737 },  { 30051, 4737 },
		{ 30052, 4737 },   { 30053, 4738 },  { 35239, 23694 }, { 35240, 23734 }, { 35320, 23825 },
		{ 35237, 23689 },  { 35238, 23689 }, { 35757, 24297 }, { 35761, 24312 }, { 35759, 24311 },
		{ 35145, 25205 },  { 37071, 1646 },  { 34107, 17152 }, { 34106, 17151 }, { 31765, 197 },
		{ 36744, 2507 },   { 36699, 2506 },  { 36698, 2505 },  { 36697, 2504 },  { 36696, 2503 },
		{ 33470, 207 },    { 37431, 11598 }, { 32004, 26822 }, { 35674, 26825 }, { 34796, 26824 },
		{ 35728, 26984 },  { 35729, 26985 }, { 35730, 26986 }, { 35731, 26987 }, { 35732, 26988 },
		{ 35735, 26994 },  { 35736, 26995 }, { 35737, 10276 }, { 35738, 10275 }, { 36737, 26938 },
		{ 36835, 26826 },  { 35733, 26989 }, { 31772, 2306 },  { 31773, 2307 },  { 31774, 2308 },
		{ 35318, 23824 },  { 5585, 15508 },  { 36919, 1551 },  { 36917, 1553 },  { 33945, 11559 },
		{ 33948, 11558 },  { 35288, 4242 },  { 29340, 660 },   { 37047, 380 },   { 31766, 25834 },
		{ 33405, 25833 },  { 33406, 25835 }, { 33456, 196 },   { 33980, 26089 }, { 33981, 26089 },
		{ 33982, 26089 },  { 33983, 26089 }, { 33984, 26089 }, { 33986, 26089 }, { 33989, 26098 },
		{ 33995, 25890 },  { 33988, 26092 }, { 33996, 25895 }, { 33997, 25899 }, { 33998, 25903 },
		{ 35908, 26018 },  { 35915, 26019 }, { 36734, 925 },   { 33985, 26089 }, { 33434, 3770 },
		{ 35355, 1248 },   { 32334, 14427 }, { 32338, 14427 }, { 37298, 1106 },  { 30503, 17600 },
		{ 31749, 796 },    { 33972, 3389 },  { 33973, 5812 },  { 33453, 25795 }, { 33454, 25795 },
		{ 34206, 2215 },   { 34207, 2215 },  { 33499, 2727 },  { 37158, 590 },   { 37159, 591 },
		{ 34205, 2214 },   { 33931, 350 },   { 37164, 598 },   { 37167, 604 },   { 33969, 25656 },
		{ 33970, 23657 },  { 33971, 25658 }, { 37302, 1023 },  { 33469, 206 },   { 34243, 24811 },
		{ 34244, 24812 },  { 34246, 24670 }, { 36705, 1116 },  { 36706, 1116 },  { 37122, 693 },
		{ 35297, 23801 },  { 35298, 23801 }, { 30797, 17092 }, { 30798, 17094 }, { 34267, 4256 },
		{ 33455, 11816 },  { 35768, 11828 }, { 35769, 11828 }, { 35770, 11828 }, { 35771, 14395 },
		{ 35772, 14395 },  { 35786, 11775 }, { 35787, 11775 }, { 35781, 11732 }, { 35782, 11733 },
		{ 35784, 11744 },  { 35785, 11745 }, { 35793, 11966 }, { 35794, 11966 }, { 35795, 11966 },
		{ 35796, 11966 },  { 35797, 11966 }, { 35798, 11966 }, { 35814, 11652 }, { 35783, 11735 },
		{ 35818, 11829 },  { 35840, 26156 }, { 35841, 26156 }, { 35843, 26157 }, { 35844, 26157 },
		{ 35845, 26157 },  { 35846, 26157 }, { 35848, 26168 }, { 35826, 11988 }, { 35827, 11988 },
		{ 35788, 11776 },  { 35789, 11776 }, { 37054, 391 },   { 37174, 1018 },  { 33463, 373 },
		{ 33500, 2637 },   { 35387, 963 },   { 33432, 26060 }, { 33433, 26066 }, { 37277, 980 },
		{ 34964, 5864 },   { 34965, 5867 },  { 34966, 5868 },  { 34967, 5869 },  { 34968, 5879 },
		{ 34264, 4256 },   { 34265, 4256 },  { 34266, 4256 },  { 34773, 186 },   { 29800, 18182 },
		{ 29801, 18179 },  { 29802, 18154 }, { 37029, 355 },   { 35358, 1134 },  { 35359, 1135 },
		{ 35360, 1137 },   { 35361, 1138 },  { 35362, 1140 },  { 35363, 1143 },  { 35970, 2313 },
		{ 37190, 487 },    { 37191, 488 },   { 34049, 6120 },  { 34050, 6121 },  { 37175, 1017 },
		{ 37179, 1018 },   { 27561, 18908 }, { 33066, 1993 },  { 33046, 1994 },  { 31743, 15902 },
		{ 31744, 15974 },  { 34156, 22334 }, { 34157, 22335 }, { 34158, 22336 }, { 34159, 22337 },
		{ 34160, 22305 },  { 34161, 22306 }, { 34162, 22307 }, { 34163, 22308 }, { 34165, 22309 },
		{ 34167, 22310 },  { 34169, 22311 }, { 34171, 22312 }, { 34172, 22313 }, { 34174, 22314 },
		{ 34176, 22314 },  { 34178, 22315 }, { 34182, 22310 }, { 34183, 22311 }, { 34184, 22312 },
		{ 34185, 22313 },  { 34186, 22314 }, { 34187, 22315 }, { 34154, 22033 }, { 34155, 22033 },
		{ 34203, 22430 },  { 34204, 22431 }, { 35226, 17319 }, { 35227, 17319 }, { 35228, 17347 },
		{ 1082, 18493 },   { 35156, 419 },   { 37031, 358 },   { 37157, 885 },   { 37166, 602 },
		{ 37300, 1012 },   { 37319, 1011 },  { 36389, 1620 },  { 36405, 1622 },  { 35952, 24939 },
		{ 35031, 17780 },  { 30267, 17608 }, { 30268, 17609 }, { 30269, 17610 }, { 30270, 17611 },
		{ 30271, 17612 },  { 35020, 17512 }, { 35022, 17512 }, { 35023, 17512 }, { 30453, 17748 },
		{ 30454, 17747 },  { 30523, 17912 }, { 30522, 17911 }, { 30272, 17900 }, { 30298, 17900 },
		{ 36763, 19694 },  { 36688, 1336 },  { 36689, 1336 },  { 36690, 1336 },  { 34150, 5239 },
		{ 34082, 20358 },  { 35383, 417 },   { 35384, 419 },   { 37150, 5609 },  { 34240, 6601 },
		{ 34242, 6601 },   { 34239, 6564 },  { 34238, 6590 },  { 31359, 6249 },  { 35801, 196 },
		{ 37347, 18901 },  { 37348, 18901 }, { 37349, 18905 }, { 33961, 17104 }, { 35229, 17349 },
		{ 35222, 17302 },  { 35224, 17382 }, { 34273, 15097 }, { 34925, 15101 }, { 34926, 15102 },
		{ 34927, 16974 },  { 34928, 16975 }, { 34929, 16976 }, { 34931, 16986 }, { 34932, 16861 },
		{ 34934, 16883 },  { 34936, 16894 }, { 34937, 16901 }, { 34938, 16901 }, { 34940, 16914 },
		{ 16791, 16787 },  { 29994, 16788 }, { 29996, 16790 }, { 29995, 16790 }, { 29993, 16788 },
		{ 16790, 16787 },  { 29999, 16792 }, { 30000, 16794 }, { 30001, 16795 }, { 29997, 16791 },
		{ 29998, 16791 },  { 34933, 16878 }, { 34935, 16892 }, { 36306, 16736 }, { 34924, 16967 },
		{ 30003, 21332 },  { 30004, 21328 }, { 30005, 21329 }, { 30006, 21504 }, { 30009, 21434 },
		{ 30008, 21433 },  { 35100, 21526 }, { 33485, 25281 }, { 33487, 25283 }, { 37090, 25352 },
		{ 35865, 20175 },  { 29956, 16134 }, { 34025, 16059 }, { 34026, 16060 }, { 34027, 16061 },
		{ 34028, 16062 },  { 33107, 16058 }, { 33132, 16472 }, { 33131, 16470 }, { 33130, 16471 }
	};
}