package faubiguy.cityclaims;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CityFlags extends Flags {

	public static final CityFlags DEFAULTS;
	public static final Map<String,String> FLAG_TYPES;
	
	public final City city;
	
	public PlotType.TypeFlags typeFlags = new PlotType.TypeFlags(null);
	
	static {
		Map<String,Object> defaultsMap = new HashMap<String, Object>();
		defaultsMap.put("hidden", false);
		DEFAULTS = new CityFlags(null, defaultsMap);		
		FLAG_TYPES = new HashMap<String, String>();
		FLAG_TYPES.put("hidden", "boolean");
		
	}
	
	public Map<String,String> getFlagTypes() {
		return FLAG_TYPES;
	}

	public CityFlags(City city, Map<String, Object> map) { // Construct from Map of flags
		super(FLAG_TYPES);
		this.city = city; 
		flags = map;
		inheritFrom = CityClaims.instance.defaults;
	}

	public CityFlags(City city) { // Construct without any flags
		this(city, new HashMap<String,Object>());
	}
	
	public boolean hasCity() {
		return city != null;
	}
	
	public List<String> listFlags(boolean includeLocked) {
		List<String> flagList = new ArrayList<String>();
		for (String flag : getFlagTypes().keySet()) {
			if (includeLocked || !CityClaims.instance.lockedFlags.contains(flag)) {
				flagList.add(flag);
			}
		}
		return flagList;
	}
	
	public Object getFlag(String flag) {
		return getFlag(flag, true);
	}

	public Object getFlag(String flag, boolean inherit) {		
		//CityClaims.instance.getLogger().info("Getting flag: " + flag);
		Object value;
		if (typeFlags.isFlag(flag)) {
			value = typeFlags.getFlag(flag);
		} else {
			value = flags.get(flag);
		}
		if (!inherit) {
			//CityClaims.instance.getLogger().info("Inherit is false");
			//CityClaims.instance.getLogger().info("Returning " + (value != null ? value.toString() : "null"));
			return value;
		}
		if (value == null) {
			//CityClaims.instance.getLogger().info("Checking server default");
			value = CityClaims.instance.defaults.getFlagNoInherit(flag);
			if (value == null) {
				//CityClaims.instance.getLogger().info("Checking plugin default");
				value = getDefaults().getFlagNoInherit(flag);
			}
		}
		//CityClaims.instance.getLogger().info("Returning (inherited)" + value.toString());
		return value;
	}
	
	public boolean setFlag(String flag, Object value) {
		return setFlag(flag, value, false);
	}

	public boolean setFlag(String flag, Object value, boolean overrideLock) {
		if (!overrideLock && CityClaims.instance.lockedFlags.contains(flag) && flags.get(flag) == null) {
			return false;
		}
		if (typeFlags.isFlag(flag)) {
			typeFlags.setFlag(flag, value);
		} else {
			flags.put(flag, value);
		}
		return true;
	}
	
	public void setFlags(Map<String,Object> flags) {
		setFlags(flags, false);
	}
	
	public void setFlags(Map<String,Object> flags, boolean overrideLock) {
		for (Map.Entry<String, Object> entry : flags.entrySet()) {
			String flag = entry.getKey();
			if (!overrideLock && CityClaims.instance.lockedFlags.contains(flag) && flags.get(flag) == null) {
				return;
			}
			setFlag(flag, entry.getValue());
		}
	}
	
	public static void loadGlobalFlags() {
		FileConfiguration flagFile = YamlConfiguration
				.loadConfiguration(new File(CityClaims.instance.dataPath,
						"flags.yml"));
		if(flagFile.isConfigurationSection("flags")) {
			CityClaims.instance.defaults = new CityFlags(null, flagFile
					.getConfigurationSection("flags").getValues(false));
		} else {
			CityClaims.instance.defaults = new CityFlags(null);
		}
		if (flagFile.isConfigurationSection("locked")) {
			CityClaims.instance.lockedFlags = new HashSet<String>(flagFile
					.getConfigurationSection("locked").getStringList(""));
		} else {
			CityClaims.instance.lockedFlags = new HashSet<String>();
		}
	}

	public static boolean saveGlobalFlags() {
		FileConfiguration flagFile = YamlConfiguration
				.loadConfiguration(new File(CityClaims.instance.dataPath,
						"flags.yml"));
		flagFile.createSection("flags", CityClaims.instance.defaults.getMap());
		flagFile.set("locked", new ArrayList<String>(CityClaims.instance.lockedFlags));
		try {
			flagFile.save(new File(CityClaims.instance.dataPath, "flags.yml"));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void save() {
		city.saveFlags();
		
	}

	@Override
	public Flags getDefaults() {
		return DEFAULTS;
	}
	
	public Map<String,Object> getMap() {
		Map<String,Object> map = new HashMap<String, Object>();
		for (Map.Entry<String,Object> entry : flags.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String,Object> entry : typeFlags.getMap().entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

}
