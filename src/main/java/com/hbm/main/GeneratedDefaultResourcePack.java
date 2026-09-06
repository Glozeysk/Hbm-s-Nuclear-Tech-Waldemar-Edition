package com.hbm.main;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

/**
 * Synthesizes the boilerplate blockstate/model JSON for the mod's own assets when no hand-written file exists, so a
 * plain full-cube block or a flat item needs no JSON at all - just a texture named after the registry name.
 *
 * It only ever fills gaps: if the real file is present on the classpath this pack reports the resource as absent and
 * the mod's own file pack serves it, so every existing asset is untouched. Only these flat patterns are generated:
 *   blockstates/NAME.json     -> normal variant pointing at hbm:NAME
 *   models/block/NAME.json    -> block/cube_all textured with hbm:blocks/NAME
 *   models/item/NAME.json     -> parent hbm:block/NAME if NAME is a block, else item/generated with hbm:items/NAME
 * Anything with variants, orientation or multiple layers still ships a hand-written JSON, which wins by existing.
 */
public class GeneratedDefaultResourcePack implements IResourcePack {

	private static final String ASSETS = "/assets/" + RefStrings.MODID + "/";

	@Override
	public boolean resourceExists(ResourceLocation location) {
		if(!RefStrings.MODID.equals(location.getNamespace()))
			return false;
		if(realExists(location.getPath()))
			return false; //let the mod's own file pack serve it
		return synthesize(location.getPath()) != null;
	}

	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		InputStream real = getClass().getResourceAsStream(ASSETS + location.getPath());
		if(real != null)
			return real;
		String json = synthesize(location.getPath());
		if(json == null)
			throw new FileNotFoundException(location.getPath());
		return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
	}

	private boolean realExists(String path) {
		InputStream in = getClass().getResourceAsStream(ASSETS + path);
		if(in == null)
			return false;
		try { in.close(); } catch(IOException e) { }
		return true;
	}

	//returns the generated JSON for a supported flat path, or null if not synthesizable. Generation is gated on a real
	//texture (or real model) existing, so it only ever fills in content that ships a matching texture - programmatic
	//blocks that intentionally ship no blockstate/texture are left untouched.
	private String synthesize(String path) {
		if(!path.endsWith(".json"))
			return null;

		String name = between(path, "blockstates/", ".json");
		if(name != null && isFlat(name) && blockModelAvailable(name))
			return "{\"variants\":{\"normal\":{\"model\":\"" + RefStrings.MODID + ":" + name + "\"}}}";

		name = between(path, "models/block/", ".json");
		if(name != null && isFlat(name) && realExists("textures/blocks/" + name + ".png"))
			return "{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\"" + RefStrings.MODID + ":blocks/" + name + "\"}}";

		name = between(path, "models/item/", ".json");
		if(name != null && isFlat(name)) {
			if(isBlock(name) && blockModelAvailable(name))
				return "{\"parent\":\"" + RefStrings.MODID + ":block/" + name + "\"}";
			if(!isBlock(name) && realExists("textures/items/" + name + ".png"))
				return "{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"" + RefStrings.MODID + ":items/" + name + "\"}}";
		}
		return null;
	}

	//a block model can be provided if a real one ships or we can synthesize a cube_all from a matching texture
	private boolean blockModelAvailable(String name) {
		return realExists("models/block/" + name + ".json") || realExists("textures/blocks/" + name + ".png");
	}

	private static String between(String path, String prefix, String suffix) {
		if(path.startsWith(prefix) && path.endsWith(suffix))
			return path.substring(prefix.length(), path.length() - suffix.length());
		return null;
	}

	//only flat single-segment names get a default; nested/variant paths are left to hand-written files
	private static boolean isFlat(String name) {
		return !name.isEmpty() && name.indexOf('/') < 0;
	}

	private static boolean isBlock(String name) {
		return Block.REGISTRY.getObject(new ResourceLocation(RefStrings.MODID, name)) != Blocks.AIR;
	}

	@Override
	public Set<String> getResourceDomains() {
		return Collections.singleton(RefStrings.MODID);
	}

	@Nullable
	@Override
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
		return null;
	}

	@Override
	public BufferedImage getPackImage() throws IOException {
		throw new FileNotFoundException("pack.png");
	}

	@Override
	public String getPackName() {
		return "HBM Generated Defaults";
	}
}
