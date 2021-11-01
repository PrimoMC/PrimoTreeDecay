package net.primomc.PrimoTreeDecay;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright 2021 github.com/kukelekuuk
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

    @EventHandler( priority = EventPriority.MONITOR, ignoreCancelled = true )
    public void onBlockBreak( BlockBreakEvent event )
    {
        if ( isLog( event.getBlock() ) )
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
                        if ( isLeaves( block ) )
                        {
                            Leaves data = (Leaves) block.getState().getBlockData();
                            if ( data.isPersistent() )
                            {
                                continue;
                            }
                            if ( data.getDistance() <= 6 )
                            {
                                continue;
                            }
                            decay.add( block.getState() );

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

    private boolean isLeaves( Block block )
    {
        return block.getState().getBlockData() instanceof Leaves;
    }

    private boolean isLog( Block block )
    {
        return block.getType().toString().contains( "_LOG" );
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
                    if ( isLeaves( block ) )
                    {
                        block.breakNaturally();
                    }
                }
                currentlySelectedDecayList.removeFirst();
            }
        }
    }
}
