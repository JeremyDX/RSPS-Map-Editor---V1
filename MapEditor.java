public class MapEditor
{
	public static void loadFile()
	{
		Configurations.loadObjectDefinitions();
		Configurations.LOCK_OBJECT_REVISION(474);
		Configurations.MAP_INDEX = Configurations.getMapIndexFromPrompt(false);

		ForceExtraObjects(); //Call this first to ensure these objects are forced into the map!

		loadFile(Configurations.getRevisionFile("377"));

		MapWriter.create(); //Create our finalized map!
	}

	/* QUICK-FIND-CODE: Extras
	 * MapWriter.add(ID, TYPE, FACE, X, Y, Z);
	 * Utilize this to demand these objects are inserted into the map regardless!
	 * Warning there are NO Security Checks! E.g. Swap, Remove, Block aren't enabled for this!
	 */
	private static void ForceExtraObjects()
	{
	
	}

	private static void DisplayObjectInformation(int id, int type, int face, int x, int y, int z)
	{
		if (false)
		{
			MapWriter.print("[DISPLAY]", id, type, face, x, y, z);		
		}
	}

	public static boolean RemoveObjects(int id, int type, int face, int x, int y, int z)
	{
		return false;
	}

	private static void loadFile(ReadWriteBuffer map_file)
	{
		if (map_file == null || map_file.capacity() <= 0)
		{
			System.err.println((new StringBuilder()).append("Failed Loading MapIndex: ").
				append(Configurations.MAP_INDEX).append(", Revision Loaded: ").
				append(Configurations.getReadingVersion()).append(", FileSize: ").
				append(map_file == null ? -1 : map_file.capacity()).toString());
			return;
		}

		int object_id = -1;

		ForceExtraObjects();

		while(true) 
		{
			int object_increase = map_file.readMedium();
			if (object_increase < 1)
				break;
			object_id += object_increase;

			int location_data = 0;
			while(true)
			{
				int location_increase = map_file.readMedium();
				if (location_increase < 1)
					break;
				location_data += location_increase - 1;
				int z = (location_data >> 12);
				int locX = (location_data >> 6) - (z << 6);
				int locY = location_data - (locX << 6) - (z << 12);

				//Final Coordinates of this object, in a (16-79) x (32-95) chunk.
				int localY = 16 + locY;
				int localX = 32 + locX;

				//Properties of this object!
				int object_properties = map_file.readByte();
				int type = object_properties >> 2;
				int face = object_properties - (type << 2);

				if (Configurations.isBlocked(object_id))
				{
					MapWriter.print("[BLOCKED]", object_id, type, face, Configurations.realX(locX), Configurations.realY(locY), z);
					continue;
				}

				if (RemoveObjects(object_id, type, face, Configurations.realX(locX), Configurations.realY(locY), z))
				{
					MapWriter.print("[REMOVE]", object_id, type, face, Configurations.realX(locX), Configurations.realY(locY), z);
					continue;
				}

				DisplayObjectInformation(object_id, type, face, Configurations.realX(locX), Configurations.realY(locY), z);
				
				int final_object_id = Configurations.getSwappedObjectID(object_id);
				if (final_object_id != object_id)
				{
					System.out.println("[SWAPPED]mapWriter.add(" + object_id + "," + type + "," + face + ","
					+ Configurations.realX(locX) + "," + Configurations.realY(locY) + "," + z + "); >> Replaced ID: " + final_object_id);
				}

				MapWriter.add(final_object_id, type, face, localX, localY, z);
			}
		}
	}

	private static ReadWriteBuffer getMapForRevision(int revision)
	{
		return ReadWriteBuffer.read((new StringBuilder()).append(Configurations.DIRECTORY).append("DATA/MapData/").append(revision).
			append("maps/").append(Configurations.MAP_INDEX).append(".dat").toString());
	}
}