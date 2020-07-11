package ca.cobiy.oreregeneration.JanTuck;

import org.bukkit.Material;

public class BlocksChances {
	public Material blocks;
	public int chances;
	
	public BlocksChances(Material blocks, int chances) {
		this.blocks = blocks;
		this.chances = chances;
	}
	
    public BlocksChances(String blocks, int chances) {
        this.blocks = Material.matchMaterial(blocks);
        this.chances = chances;
    }
    
    public Material getBlocks() {
        return blocks;
    }

    public int getChances() {
        return chances;
    }
    
}
