package com.golemsdontdie;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** What golems work at, by what the game offers a player there, and where they stand to do it. */
public class GolemWorkTest
{
	private static String[] actions(String... actions)
	{
		return actions;
	}

	@Test
	public void treesRocksAndRangesByTheirMenus()
	{
		assertEquals(GolemWork.WOODCUTTING, GolemWork.forObject("Yew tree", actions("Chop down", null, null, null, null)));
		assertEquals(GolemWork.MINING, GolemWork.forObject("Iron rocks", actions("Mine", "Prospect", null, null, null)));
		assertEquals(GolemWork.COOKING_ON_RANGE, GolemWork.forObject("Range", actions("Cook", null, null, null, null)));
		assertEquals(GolemWork.SPINNING, GolemWork.forObject("Spinning wheel", actions("Spin", null, null, null, null)));
	}

	/** A stump offers nothing, so a felled tree is never chopped. */
	@Test
	public void aStumpIsNoTree()
	{
		assertNull(GolemWork.forObject("Tree stump", actions(null, null, null, null, null)));
		assertNull(GolemWork.forObject("Door", actions("Open", null, null, null, null)));
		assertNull(GolemWork.forObject(null, null));
	}

	/** What a player uses an item on has no menu entry to say so, and is known by name. */
	@Test
	public void firesWheelsAndPatchesByName()
	{
		assertEquals(GolemWork.COOKING_ON_FIRE, GolemWork.forObject("Fire", actions(null, null, null, null, null)));
		assertEquals(GolemWork.POTTERY, GolemWork.forObject("Potter's Wheel", null));
		assertEquals(GolemWork.RAKING, GolemWork.forObject("Herb patch", actions("Inspect", "Guide", null, null, null)));
		assertEquals(GolemWork.RAKING, GolemWork.forObject("Allotment", actions("Rake", "Inspect", null, null, null)));
	}

	@Test
	public void fishingSpotsByHowTheyAreFished()
	{
		String[] netOrBait = actions("Net", "Bait", null, null, null);
		assertEquals(GolemWork.NET_FISHING, GolemWork.forFishingSpot(netOrBait, 0));
		assertEquals(GolemWork.ROD_FISHING, GolemWork.forFishingSpot(netOrBait, 1));
		assertEquals(GolemWork.CAGE_FISHING, GolemWork.forFishingSpot(actions("Cage", "Harpoon", null, null, null), 2));
		assertNull(GolemWork.forFishingSpot(actions("Talk-to", null, null, null, null), 0));
	}

	/** Every tool is a wield model, never the chisel's icon, and handiwork always has one in hand. */
	@Test
	public void handiworkHoldsATool()
	{
		for (GolemWork work : GolemWork.HANDIWORK)
		{
			assertTrue(work + " has no tool", work.getToolModel() >= 0);
		}
	}

	/** A golem stands round a spot, never on it; a two-by-two rock has twelve tiles round it. */
	@Test
	public void standingBesideASpot()
	{
		WorkSpots.Spot rock = new WorkSpots.Spot(100, 200, 0, 2, 2, GolemWork.MINING);
		int beside = 0;
		for (int x = 98; x <= 103; x++)
		{
			for (int y = 198; y <= 203; y++)
			{
				if (rock.isBeside(x, y))
				{
					beside++;
				}
			}
		}
		assertEquals(12, beside);
		assertFalse(rock.isBeside(100, 200));
		assertTrue(rock.isBeside(99, 201));
		assertFalse(rock.isBeside(97, 201));
	}

	/**
	 * A range against a house wall is worked from inside, never through the wall from out in the yard:
	 * golems stood outside the wall cooking on it. Nor from a tile something stands on.
	 */
	@Test
	public void noWorkingThroughAWall()
	{
		int[][] flags = new int[20][20];
		// The range at (10, 10); a wall on its south edge, as seen from both sides of the wall.
		flags[10][10] = net.runelite.api.CollisionDataFlag.BLOCK_MOVEMENT_OBJECT
			| net.runelite.api.CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
		flags[10][9] = net.runelite.api.CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
		// A table to its west.
		flags[9][10] = net.runelite.api.CollisionDataFlag.BLOCK_MOVEMENT_OBJECT;
		WorkSpots.Spot range = new WorkSpots.Spot(1010, 2010, 0, 1, 1, GolemWork.COOKING_ON_RANGE);
		range.setStands(WorkSpots.standsFor(range, flags, 1000, 2000));

		assertFalse("through the wall", range.isStand(1010, 2009));
		assertFalse("on the table", range.isStand(1009, 2010));
		assertTrue(range.isStand(1010, 2011));
		assertTrue(range.isStand(1011, 2010));
		assertEquals(2, range.getStands().length);
	}

	/** The first read is due at once, whatever the tick: the spots were once never read at all. */
	@Test
	public void theSceneIsReadAtOnceAndThenEverySoOften()
	{
		assertTrue(WorkSpots.due(0, Integer.MIN_VALUE, 5));
		assertTrue(WorkSpots.due(123456, Integer.MIN_VALUE, 5));
		assertFalse(WorkSpots.due(102, 100, 5));
		assertTrue(WorkSpots.due(105, 100, 5));
		assertTrue(WorkSpots.due(3, 100, 5));
	}

	@Test
	public void spotsNearAGolemOnItsFloor()
	{
		WorkSpots.Spot tree = new WorkSpots.Spot(100, 100, 0, 1, 1, GolemWork.WOODCUTTING);
		WorkSpots.Spot upstairs = new WorkSpots.Spot(100, 100, 1, 1, 1, GolemWork.COOKING_ON_RANGE);
		WorkSpots.Spot far = new WorkSpots.Spot(150, 100, 0, 1, 1, GolemWork.MINING);
		WorkSpots spots = WorkSpots.of(Arrays.asList(tree, upstairs, far));

		assertEquals(Collections.singletonList(tree), spots.near(105, 100, 0, 10, new ArrayList<>()));
		assertTrue(spots.stillThere(new WorkSpots.Spot(100, 100, 0, 1, 1, GolemWork.WOODCUTTING)));
		assertFalse(spots.stillThere(new WorkSpots.Spot(101, 100, 0, 1, 1, GolemWork.WOODCUTTING)));
	}
}
