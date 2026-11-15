package net.royling.lsp.lofty_safe_protection;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import static org.junit.jupiter.api.Assertions.*;

class MixinTargetsTest {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/";

    @Test void allClientAndServerMixinTargetsMatchThisMinecraftVersion() throws Exception {
        Path dir = Path.of(System.getProperty("lsp.testProjectDir"), "build/classes/java/main/net/royling/lsp/lofty_safe_protection/mixin");
        int checked = 0;
        try (var files = Files.list(dir)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                ClassNode mixin = read(Files.readAllBytes(file));
                var annotation = annotation(mixin.visibleAnnotations, mixin.invisibleAnnotations, MIXIN + "Mixin;");
                if (annotation == null) continue;
                for (Object type : (List<?>) value(annotation, "value")) {
                    String name = ((Type) type).getInternalName();
                    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
                        assertNotNull(stream, "Missing Minecraft class " + name);
                        verify(mixin, read(stream.readAllBytes()));
                    }
                    checked++;
                }
            }
        }
        assertTrue(checked >= 6);
    }

    private void verify(ClassNode mixin, ClassNode target) {
        String context = mixin.name + " -> " + target.name;
        for (var field : mixin.fields) {
            if (annotation(field.visibleAnnotations, field.invisibleAnnotations, MIXIN + "Shadow;") != null)
                assertTrue(target.fields.stream().anyMatch(candidate -> candidate.name.equals(field.name) && candidate.desc.equals(field.desc)), context + " shadow " + field.name);
        }
        for (var method : mixin.methods) {
            var invoker = annotation(method.visibleAnnotations, method.invisibleAnnotations, MIXIN + "gen/Invoker;");
            if (invoker != null) assertTrue(target.methods.stream().anyMatch(candidate -> candidate.name.equals(value(invoker, "value")) && candidate.desc.equals(method.desc)), context + " invoker " + method.name);
            var accessor = annotation(method.visibleAnnotations, method.invisibleAnnotations, MIXIN + "gen/Accessor;");
            if (accessor != null) {
                Type[] args = Type.getArgumentTypes(method.desc);
                String descriptor = args.length == 0 ? Type.getReturnType(method.desc).getDescriptor() : args[0].getDescriptor();
                assertTrue(target.fields.stream().anyMatch(field -> field.name.equals(value(accessor, "value")) && field.desc.equals(descriptor)), context + " accessor " + method.name);
            }
            var inject = annotation(method.visibleAnnotations, method.invisibleAnnotations, MIXIN + "injection/Inject;");
            if (inject == null) continue;
            Type[] args = Type.getArgumentTypes(method.desc);
            var prefix = new ArrayList<Type>();
            for (Type arg : args) {
                if (arg.getClassName().startsWith("org.spongepowered.asm.mixin.injection.callback.")) break;
                prefix.add(arg);
            }
            for (Object raw : (List<?>) value(inject, "method")) {
                String selector = raw.toString();
                int descriptorStart = selector.indexOf('(');
                String name = descriptorStart < 0 ? selector : selector.substring(0, descriptorStart);
                assertTrue(target.methods.stream().anyMatch(candidate -> {
                    if (!candidate.name.equals(name) || descriptorStart >= 0 && !candidate.desc.equals(selector.substring(descriptorStart))) return false;
                    Type[] actual = Type.getArgumentTypes(candidate.desc);
                    if (prefix.size() > actual.length) return false;
                    for (int i = 0; i < prefix.size(); i++) if (!prefix.get(i).equals(actual[i])) return false;
                    return true;
                }), context + " injection " + selector + " with " + prefix);
            }
        }
    }

    private static AnnotationNode annotation(List<AnnotationNode> visible, List<AnnotationNode> invisible, String descriptor) {
        for (var list : java.util.Arrays.asList(visible, invisible)) if (list != null)
            for (var node : list) if (node.desc.equals(descriptor)) return node;
        return null;
    }
    private static Object value(AnnotationNode node, String key) {
        if (node.values != null) for (int i = 0; i < node.values.size(); i += 2)
            if (key.equals(node.values.get(i))) return node.values.get(i + 1);
        return null;
    }
    private static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return result;
    }
}
