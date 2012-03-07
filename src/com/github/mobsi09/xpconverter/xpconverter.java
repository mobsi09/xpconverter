package com.github.mobsi09.xpconverter;

import java.util.HashMap;
import java.util.logging.Logger;


import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class xpconverter extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	public Integer configcost = 0;
	public Integer configratio = 7;
	private static Economy economy = null;
	
	xpconverter plugin;
	
	public void onEnable(){
		  	
        this.getConfig().options().copyDefaults(true);
        this.saveConfig(); 
        
        xpconverterconfig(); 
		
		log.info("[xpconverter] enabled");

		
        if (!setupEconomy()) {
        	this.setEnabled(false);
            System.out.println(this + " could not find Vault plugin! Disabling...");
            return;
        }
        

	}
	
	public void onDisable(){
		log.info("[xpconverter] disabled");
	}
	
    @Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    		Player player = null;
    		
    		if (sender instanceof Player) {
    			player = (Player) sender;
    		}
    		
    		if(command.getName().equalsIgnoreCase("convertxp")){

				if(player == null){
					return false;
				}else{
					if(player.hasPermission("convertxp.convert")){
	   			
		    			if(args.length > 0){
		    				try {
		    				int arg = Integer.parseInt(args[0]);		    				
		    				}catch(NumberFormatException nfe){
			    				player.sendMessage(ChatColor.DARK_AQUA + "Please enter a valid number");
			    				return true;		
		    				}	    				
		    				convertxppart(player, args[0]);
		    					return true;
		    			}else{
							convertxp(player);
							return true;   		    				
		    			}
						
					}                          
			    }
    		}else if(command.getName().equalsIgnoreCase("getxp")){
				if(player == null){
					return false;
				}else{
					if(player.hasPermission("convertxp.getxp")){
						getxp(player);
						return true;  						
					}                         
			    }
    		}else if(command.getName().equalsIgnoreCase("convertxpreload")){
					if(player.hasPermission("convertxp.convertxpreload")|| (player == null)){
						reloadconfig();
						return true;  						
					}                         
			}else if(command.getName().equalsIgnoreCase("convertxpinfo")){
				if(player == null){
					return false;
				}else{
					if(player.hasPermission("convertxp.convertxpinfo")){
						convertxpinfo(player);
						return true;  						
					}                         
			    }
    		}
    		
    		return false;
	}
    

    private void convertxpinfo(Player player) {
		player.sendMessage("You get 1 Bottle o Enchant per " + configratio + " Xp");
		player.sendMessage("Each Bottle will cost " + configcost + " to make");
	}

	public void xpconverterconfig(){    	
    	configcost = this.getConfig().getInt("MoneyCostPerBottle", 0);
    	configratio = this.getConfig().getInt("XpCostPerBottle", 7);
    }
    
    public void reloadconfig(){
    		this.reloadConfig();
    		this.getConfig().options().copyDefaults(true);
    		this.saveConfig();
    		xpconverterconfig();
    		log.info("xpconverter config has been reloaded."); 
    	
    }
    
    
	private void convertxp(Player player){
    	int xp = player.getTotalExperience();
		if(configratio<=0){
			configratio=1;
		}
    	int leftover = xp%configratio;
    	xp = xp/configratio;    	
    	final int  bottleamount = (int)xp;
    	int finalcost = configcost*bottleamount;  	
    	if(bottleamount>0){
    		if (finalcost!=0){
        		if(xpconverter.getEconomy().withdrawPlayer(player.getName(), finalcost).transactionSuccess()){
        			xpstuff(player, leftover, bottleamount);
                	player.sendMessage("Deducted " + finalcost +  " " + xpconverter.getEconomy().currencyNamePlural() + " from your Account.");
        		}else{
        			player.sendMessage("Insufficent Funds");
        		}
    			
    		}else{
    			xpstuff(player, leftover, bottleamount);	
    		}
    	}
    }
    
    private void convertxppart(Player player, String arg){
    	int amount = Integer.parseInt(arg);
    	if(amount != 0){
        	int xp = player.getTotalExperience();
        	if(amount<=xp){
        		if(configratio<=0){
        			configratio=1;
        		}
            	int leftover = amount%configratio;
            	int leftoverxp = xp-amount+leftover;
            	amount = amount/configratio;
            	final int  bottleamount = (int)amount;
            	int finalcost = configcost*bottleamount;             	
            	if(bottleamount>0){
            		if (finalcost!=0){
                		if(xpconverter.getEconomy().withdrawPlayer(player.getName(), finalcost).transactionSuccess()){
                			xpstuff(player, leftoverxp, bottleamount);
                        	player.sendMessage("Deducted " + finalcost +  " " + xpconverter.getEconomy().currencyNamePlural() + " from your Account.");
                		}else{
                			player.sendMessage("Insufficent Funds");
                		}
            		}else{
            			xpstuff(player, leftoverxp, bottleamount);	
            		}
            	}
        	}   		
    	}
    }   
    
   private void getxp(Player player){
	   player.sendMessage(ChatColor.DARK_AQUA +"Your Total Xp is: " +player.getTotalExperience());
   }

   private Boolean setupEconomy()
   {
       RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
       if (economyProvider != null) {
           setEconomy(economyProvider.getProvider());
       }

       return (getEconomy() != null);
   }
   
	public static Economy getEconomy() {
		return economy;
	}

	private static void setEconomy(Economy economy) {
		xpconverter.economy = economy;
	}
	
	private void xpstuff(Player player,Integer leftoverxp, Integer bottleamount){
		HashMap<Integer, ItemStack> leftOver = new HashMap<Integer, ItemStack>();	
		player.setTotalExperience(0);
    	player.setExp(0);
    	player.setLevel(0);
    	player.giveExp(leftoverxp);
    	ItemStack bottleoenchant = new ItemStack(384, bottleamount);
		leftOver.putAll(player.getInventory().addItem(new ItemStack(bottleoenchant)));
        if (!leftOver.isEmpty()) {
            Location loc = player.getLocation();
            player.getWorld().dropItem(loc, new ItemStack(384, leftOver.get(0).getAmount()));
        }
	
	}
}
	
	

