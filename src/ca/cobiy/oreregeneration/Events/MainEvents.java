package ca.cobiy.oreregeneration.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import ca.cobiy.oreregeneration.Main;
import ca.cobiy.oreregeneration.JanTuck.BlocksChances;
import io.netty.util.internal.ThreadLocalRandom;

public class MainEvents implements Listener {
	HashMap<Location, Integer> blocksLeft = new HashMap<>();

	public Main main;
	public MainEvents(Main main) {
		this.main = main;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		for(String regions : main.getConfig().getConfigurationSection("Regions").getKeys(false)) {
			RegionManager regionManager = main.getWorldGuard().getRegionManager(block.getWorld());
			ApplicableRegionSet regionAtLocation = regionManager.getApplicableRegions(block.getLocation());
			for(ProtectedRegion region : regionAtLocation) {
				if(region.getId().equals(regions)) {
					String blocks = main.getConfig().getString("Regions."+regions+".blocks");
					String chances = main.getConfig().getString("Regions."+regions+".chances");
					List<BlocksChances> blocksChances = getSortedBlocksChances(blocks, chances);
					String[] blocksR = blocks.split(",");
					List<String> blockList = new ArrayList<String>(Arrays.asList(blocksR));
					for(int i = 0; i < blockList.size(); i++) {
						if(block.getType().toString().equals(blockList.get(i))) {
							if(blocksLeft.get(block.getLocation()) == null) {
								blocksLeft.put(block.getLocation(), 5);
							}
							blocksLeft.put(block.getLocation(), blocksLeft.get(block.getLocation())-1);
							Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() { public void run() { block.getLocation().getBlock().setType(getRandomBlock(blocksChances)); } }, 1L);
							if(blocksLeft.get(block.getLocation()) == 0) {
								Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() { public void run() { block.getLocation().getBlock().setType(Material.BEDROCK); } }, 1L);
								Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() { public void run() { block.getLocation().getBlock().setType(getRandomBlock(blocksChances)); } }, 20L*8);
								blocksLeft.put(block.getLocation(), 5);
							}
						}
					}
				}
			}
		}
	}
	
	public static Material getRandomBlock(List<BlocksChances> blocksChances) {
		int randomInt = ThreadLocalRandom.current().nextInt(100)+1;
		int sum = blocksChances.get(0).getChances();
		for(int i = 0; i < blocksChances.size(); i++) {
			BlocksChances materialChance = blocksChances.get(i);
			if(i>0) {
				sum += materialChance.chances;
			}
			if(randomInt <= sum) {
				return materialChance.blocks;
			}
		}
		return blocksChances.get(blocksChances.size()-1).blocks;
	}
	
	public static List<BlocksChances> getSortedBlocksChances(String blocks, String chances){
		String[] block = blocks.split(",");
		String[] chance = chances.split(",");
		BlocksChances[] blocksChances = new BlocksChances[block.length];
		for(int i = 0; i < block.length; i++) {
			blocksChances[i] = new BlocksChances(block[i], Integer.valueOf(chance[i]));
		}
		return Stream.of(blocksChances).sorted(Comparator.comparing(BlocksChances::getChances)).collect(Collectors.toList());
	}
}
