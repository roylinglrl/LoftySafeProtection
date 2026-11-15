package net.royling.lsp.lofty_safe_protection.client.renderer;
import net.royling.lsp.lofty_safe_protection.data.SafeBoxManager;
import net.royling.lsp.lofty_safe_protection.data.InventoryContainerManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import java.util.function.Consumer;
public final class SafeBoxItemRenderer implements SpecialModelRenderer<Identifier> {
    private final ModelPart model;
    public SafeBoxItemRenderer(){var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("face",CubeListBuilder.create().texOffs(0,0).addBox(0,0,8,16,16,0),PartPose.ZERO);model=LayerDefinition.create(mesh,16,16).bakeRoot();}
    @Override public Identifier extractArgument(ItemStack stack){if(stack.is(InventoryContainerManager.ITEM)){var def=InventoryContainerManager.definition(stack);return def==null?Identifier.withDefaultNamespace("textures/item/bundle.png"):def.texture();}var def=SafeBoxManager.definition(stack);return def==null?SafeBoxManager.id("textures/safebox/safe_box_default.png"):def.texture();}
    @Override public void submit(Identifier texture,PoseStack pose,SubmitNodeCollector collector,int light,int overlay,boolean foil,int outline){collector.submitModelPart(model,pose,RenderTypes.entityCutout(texture),light,overlay,null,-1,outline);}
    @Override public void getExtents(Consumer<Vector3fc> output){model.getExtentsForGui(new PoseStack(),output);}
    public record Unbaked() implements SpecialModelRenderer.Unbaked<Identifier> {
        public static final MapCodec<Unbaked> CODEC=MapCodec.unit(new Unbaked());
        @Override public MapCodec<Unbaked> type(){return CODEC;}
        @Override public SafeBoxItemRenderer bake(SpecialModelRenderer.BakingContext context){return new SafeBoxItemRenderer();}
    }
}
