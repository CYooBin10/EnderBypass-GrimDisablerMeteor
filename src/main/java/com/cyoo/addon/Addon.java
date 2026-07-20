package com.cyoo.addon;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.cyoo.addon.modules.Discord;
import com.cyoo.addon.modules.GrimDisabler;
import com.cyoo.addon.modules.TridentFly;
import com.cyoo.addon.modules.GrimAura;
import com.cyoo.addon.modules.GrimSpeed;
import com.cyoo.addon.modules.VulcanBypass;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;


public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("EnderBypass");

    @Override
    public void onInitialize() {
        LOG.info("Initializing EnderBypass Addon.");
        Modules.get().add(new GrimDisabler());
        Modules.get().add(new TridentFly());
        Modules.get().add(new GrimAura());
        Modules.get().add(new GrimSpeed());
        Modules.get().add(new VulcanBypass());
        Modules.get().add(new Discord());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.cyoo.addon";
    }

}
