package hokekyo1210.spigot.plugin.digall

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.java.JavaPlugin
import sun.audio.AudioPlayer.player
import java.util.*
import kotlin.collections.ArrayList


class DigAll : JavaPlugin(), Listener {
    override fun onEnable() { // Plugin startup logic
        server.pluginManager.registerEvents(this, this);
    }

    override fun onDisable() { // Plugin shutdown logic
    }

    val brakableBlockTypes = arrayOf(
            Material.COAL_ORE,
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG
    );

    val DIRX = arrayOf(0,0,0,0,1,-1);
    val DIRY = arrayOf(1,-1,0,0,0,0);
    val DIRZ = arrayOf(0,0,1,-1,0,0);

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent){
        val player: Player = event.getPlayer();//not null
        val blockType: Material = event.block.getType();
        val world: World = player.getWorld();
        var tool: ItemStack = player.inventory.getItemInMainHand();

        if(tool.type == Material.AIR)return;
        if(!event.isDropItems())return;
        if(!brakableBlockTypes.contains(blockType))return;
        //幅優先探索を実行
        var que = LinkedList<BlockAt>();
        var brokenBlocks = ArrayList<BlockAt>();
        que.add(BlockAt(
                event.block.getX(),
                event.block.getY(),
                event.block.getZ()
        ));
        while(!que.isEmpty()){
            val at: BlockAt = que.remove();
            val targetLocation: Location = Location(world, at.x.toDouble(), at.y.toDouble(), at.z.toDouble());
            val targetBlock: Block = targetLocation.getBlock();
            if(blockType != targetBlock.getType())continue;
            if(brokenBlocks.contains(at))continue;
            //隣接するブロックをここで破壊
            targetBlock.breakNaturally();
            brokenBlocks.add(at);
            if(!incrementToolDamage(tool)){//ツールにダメージを与える
                //ツール破壊
                player.inventory.remove(tool);
                break;
            }
            for(i in 0..5){
                que.add(BlockAt(
                        at.x+DIRX[i],
                        at.y+DIRY[i],
                        at.z+DIRZ[i]
                ));
            }
        }
    }

    fun incrementToolDamage(tool: ItemStack): Boolean {
        val itemMeta = tool.getItemMeta();
        if (itemMeta is Damageable) {
            val damageable: Damageable = itemMeta;
            val nowDamage = damageable.getDamage();
            if(tool.type.getMaxDurability() < (nowDamage+1)){
                //耐久の上限に達したらfalse返す
                return false;
            }else{
                damageable.setDamage(nowDamage+1);
            }
        }
        tool.setItemMeta(itemMeta);
        return true;
    }

    data class BlockAt(val x: Int, val y: Int, val z: Int)
}