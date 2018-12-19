public class MapWriter 
{

	private static final int[] TYPE_OVERLAP_BITS =
	{
		15,15,15,15,496,496,496,496,496,4193792,4193792,4193792,4193792,4193792,
		4193792,4193792,4193792,4193792,4193792,4193792,4193792,4193792,4194304
	};

	private static MapFile[] mapFile = new MapFile[128];
	private static ReadWriteBuffer mapPacket;
	private static int lastObjectId = -1;
	private static int lastLocId = 0;
	private static int originalLocId = 0;
	private static int curMapIndex = 0;

	protected static void create() 
	{
		mapPacket = new ReadWriteBuffer(curMapIndex * 12);
		for (int i = 0; i < curMapIndex; i++)
			writeObject(mapFile[i], i);
		mapPacket.write((new StringBuilder()).append(Configurations.DIRECTORY).append("data/newMaps/").
			append(Configurations.MAP_INDEX).append(".DAT").toString());
		System.out.println("Finished Repacking MapFile [" + Configurations.MAP_INDEX + "] - New Length: " + (mapPacket.writerIndex() - 1) + " Total Objects: " + (curMapIndex - 1));
	}

	public static void add(int xoff, int yoff, int id, int type, int face, int x, int y, int z) 
	{
		add(id, type, face, x + xoff, y + yoff, z);
	}

	public static void add(int id, int type, int face, int x, int y, int z) 
	{
		if (x > 95 || y > 79) 
		{
			if (x < Configurations.getMapX() || x > (Configurations.getMapX() + 63)) {
				System.out.println("Object's Coordinates: [" + x + " , " + y + " , " + z + "] came out of bounds for this map!");
				return;
			}
			if (y < Configurations.getMapY() || y > (Configurations.getMapY() + 63)) {
				System.out.println("Object's Coordinates: [" + x + " , " + y + " , " + z + "] came out of bounds for this map!");
				return;
			}
		}
		if (x > 95) {
			x = Configurations.locX(x);
		}
		if (y > 79) {
			y = Configurations.locY(y);
		}
		checkCapacity();
		if (containsMap(id, type, face, x, y, z))	
			return;
		for (int i = 0; i < curMapIndex; i++) 
		{
			if (mapFile[i].id >= id) 
			{
				int slot = i;
				if (mapFile[i].id == id) {
					int next = i;
					int loc = z << 12 | (x - 32) << 6 | (y - 16);
					while(mapFile[++next] != null) {
						if (mapFile[next].id != id)
							break;
						if (mapFile[next].loc > loc)
							break;
					}
					slot = next - 1 >= 0 && mapFile[next - 1].loc > loc ? next - 1 : next;
				}
				for (int grabIndex = curMapIndex; grabIndex >= slot; --grabIndex)
					mapFile[grabIndex + 1] = mapFile[grabIndex];
				mapFile[slot] = new MapFile(id, type, face, x, y, z);
				curMapIndex++;
				return;
			}
		}
		mapFile[curMapIndex] = new MapFile(id, type, face, x, y, z);
		curMapIndex++;
	}

	private static boolean containsMap(int id, int type, int face, int x, int y , int z) 
	{
		int location = z << 12 | (x - 32) << 6 | (y - 16);
		byte data = (byte) (type << 2 | face);
		for (MapFile mf : mapFile) {
			if (mf == null)
				break;
			if (mf.id == id && mf.loc == location && mf.data == data) 
			{
				System.out.println("[BLOCKED DUPLICATE] >> " + "mapWriter.add(" + id + "," + type + "," + face + "," + (Configurations.realX(x) - 32) + "," + (Configurations.realY(y) - 16) + "," + z + ");");
				return true;
			}
			if (mf.loc == location) 
			{
				int mftype = mf.data >> 2;
				if ((TYPE_OVERLAP_BITS[mftype] & (1 << type)) == (1 << type)) 
				{
					System.out.println("[BLOCKED SIMILAR] >> " + "mapWriter.add(" + id + "," + type + "," + face + "," + (Configurations.realX(x) - 32) + "," + (Configurations.realY(y) - 16) + "," + z + "); KEEP Id: " + mf.id + " Type: " + mftype);
					return true;
				}
				
			}
		}
		return false;
	}

	public static void print(String tag, int id, int type, int face, int x, int y, int z)
	{
		System.out.println((new StringBuilder()).
			append(tag).append("MapWriter.add(").append(id).
			append(", ").append(type).append(", ").append(face).
			append(", ").append(x).append(", ").append(y).
			append(", ").append(z).append("); ").toString());
	}

	private static void checkCapacity() 
	{
		if (curMapIndex < mapFile.length - 1) 
			return;

		MapFile[] maps = new MapFile[mapFile.length + 128];
		System.arraycopy(mapFile, 0, maps, 0, mapFile.length);
		mapFile = maps;
	}

	private static void writeObject(MapFile mf, int mfIndex) 
	{
		if (mapFile[mfIndex + 1] != null) 
		{
			if (mf.id != lastObjectId) 
			{
				if (lastObjectId != -1)
					mapPacket.writeByte((byte)0);

				mapPacket.writeMedium(mf.id - lastObjectId); 
				mapPacket.writeMedium(mf.loc + 1);
				mapPacket.writeByte(mf.data);
				lastLocId = 0;
				lastObjectId = mf.id;
				originalLocId = mf.loc;
			} else {
				mapPacket.writeMedium(mf.loc + 1 - lastLocId - originalLocId);
				mapPacket.writeByte(mf.data);
				lastLocId = mf.loc;
				originalLocId = 0;
			}
		} else {
			if (mapFile[mfIndex - 1].id != mf.id) 
			{
				if (lastObjectId != -1)
					mapPacket.writeByte((byte)0);
				mapPacket.writeMedium(mf.id - lastObjectId); 
				mapPacket.writeMedium(mf.loc + 1);
				mapPacket.writeByte(mf.data);
				lastLocId = 0;
				lastObjectId = mf.id;
				originalLocId = mf.loc;
			} else {
				mapPacket.writeMedium(mf.loc + 1 - lastLocId - originalLocId);
				mapPacket.writeByte(mf.data);
				lastLocId = mf.loc;
				originalLocId = 0;
			} 
			mapPacket.writeByte((byte)0);
			mapPacket.writeByte((byte)0);
		}
	}
}

class MapFile 
{
	protected int id;
	protected int loc;
	protected byte data;

	protected MapFile(int id, int t, int f, int x, int y, int z) {
		this.id = id;
		loc = z << 12 | (x - 32) << 6 | (y - 16);
		data = (byte) (t << 2 | f);
	}

}