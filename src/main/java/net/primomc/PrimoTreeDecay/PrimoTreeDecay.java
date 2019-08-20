package net.primomc.PrimoTreeDecay;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Copyright ${year} Luuk Jacobs
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class PrimoTreeDecay extends JavaPlugin implements Listener
{
    private LinkedList<List<BlockState>> decayQueue = new LinkedList<>();

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents( this, this );

        for ( int i = 0; i < 10; i++ )
        {
            new DecayTask().runTaskTimer( this, 100, 2 );
        }
    }

    private List<Material> logs = Arrays.asList( Material.OAK_LOG, Material.DARK_OAK_LOG, Material.BIRCH_LOG, Material.ACACIA_LOG, Material.JUNGLE_LOG, Material.SPRUCE_LOG, Material.STRIPPED_OAK_LOG, Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_SPRUCE_LOG );
    private List<Material> leaves = Arrays.asList( Material.OAK_LEAVES, Material.DARK_OAK_LEAVES, Material.BIRCH_LEAVES, Material.ACACIA_LEAVES, Material.JUNGLE_LEAVES, Material.SPRUCE_LEAVES );
    private final BlockFace[] directions = new BlockFace[]{ BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP };

    @EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = true )
    public void onBlockBreak( BlockBreakEvent event )
    {
        if ( logs.contains( event.getBlock().getType() ) )
        {
            if ( !event.getPlayer().getGameMode().equals( GameMode.CREATIVE ) )
            {
                event.getBlock().breakNaturally();
            }
            else
            {
                event.getBlock().setType( Material.AIR );
            }
            List<BlockState> decay = new LinkedList<>();
            for ( int x = -5; x <= 5; x++ )
            {
                for ( int y = -5; y <= 5; y++ )
                {
                    for ( int z = -5; z <= 5; z++ )
                    {
                        Block block = event.getBlock().getLocation().clone().add( x, y, z ).getBlock();
                        if ( leaves.contains( block.getType() ) )
                        {
                            if ( !isConnectedToLog( block, 0 ) )
                            {
                                if ( block.getState().getBlockData() instanceof Leaves )
                                {
                                    Leaves data = (Leaves) block.getState().getBlockData();
                                    if ( data.isPersistent() )
                                    {
                                        continue;
                                    }
                                    decay.add( block.getState() );
                                }
                            }
                        }
                    }
                }
            }
            if ( decay.isEmpty() )
            {
                return;
            }

            Collections.sort( decay, new Comparator<BlockState>()
            {
                public int compare( BlockState o1, BlockState o2 )
                {
                    return o2.getLocation().getBlockY() - o1.getLocation().getBlockY();
                }
            } );
            Collections.shuffle( decay.subList( decay.size() > 4 ? 4 : 0, decay.size() ) );
            decayQueue.add( decay );
        }
    }

    private boolean isConnectedToLog( Block block, int i )
    {
        if ( i >= 4 )
        {
            return false;
        }
        for ( BlockFace face : directions )
        {
            Block relative = block.getRelative( face );
            if ( logs.contains( relative.getType() ) )
            {
                return true;
            }
            if ( leaves.contains( relative.getType() ) )
            {
                if ( isConnectedToLog( relative, i + 1 ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    private class DecayTask extends BukkitRunnable
    {
        LinkedList<List<BlockState>> currentlySelectedDecayList = new LinkedList<>();

        @Override
        public void run()
        {
            if ( currentlySelectedDecayList.isEmpty() )
            {
                if ( decayQueue.isEmpty() )
                {
                    return;
                }
                List<BlockState> states = decayQueue.getFirst();
                currentlySelectedDecayList = new LinkedList<>( Lists.partition( states, 2 ) );
                decayQueue.removeFirst();
            }
            else
            {
                List<BlockState> states = currentlySelectedDecayList.getFirst();
                for ( BlockState state : states )
                {
                    Block block = state.getBlock();
                    if ( leaves.contains( block.getType() ) )
                    {
                        block.breakNaturally();
                    }
                }
                currentlySelectedDecayList.removeFirst();
            }
        }
    }
}
