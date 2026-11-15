package net.royling.lsp.lofty_safe_protection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "lofty_safe_protection")
public final class ForgeIntegrationTests {
    @SubscribeEvent
    public static void run(ServerStartedEvent event) throws Exception {
        if (!Boolean.getBoolean("lsp.integrationTests")) return;
        try {
            var project = Path.of(System.getProperty("lsp.testProjectDir"));
            var source = project.resolve("src/test/java");
            var builder = LauncherDiscoveryRequestBuilder.request();
            try (var files = Files.walk(source)) {
                files.filter(file -> file.toString().endsWith("Test.java")).forEach(file -> {
                    String name = source.relativize(file).toString().replace('\\', '.').replace('/', '.').replace(".java", "");
                    builder.selectors(DiscoverySelectors.selectClass(name));
                });
            }
            var listener = new SummaryGeneratingListener();
            var config = LauncherConfig.builder().enableTestEngineAutoRegistration(false)
                    .enableLauncherSessionListenerAutoRegistration(false)
                    .enableLauncherDiscoveryListenerAutoRegistration(false)
                    .enableTestExecutionListenerAutoRegistration(false)
                    .addTestEngines(new JupiterTestEngine()).build();
            LauncherFactory.create(config).execute(builder.build(), listener);
            var summary = listener.getSummary();
            var text = new StringWriter();
            summary.printTo(new PrintWriter(text));
            summary.printFailuresTo(new PrintWriter(text));
            Path report = project.resolve("build/reports/integration-tests");
            Files.createDirectories(report);
            Files.writeString(report.resolve("results.txt"), text.toString(), StandardCharsets.UTF_8);
            String json = "{\"tests\":" + summary.getTestsFoundCount() + ",\"succeeded\":" + summary.getTestsSucceededCount() + ",\"failed\":" + summary.getTestsFailedCount() + ",\"containersFailed\":" + summary.getContainersFailedCount() + "}";
            Files.writeString(report.resolve("results.json"), json, StandardCharsets.UTF_8);
            System.out.println(text);
        } finally { event.getServer().halt(false); }
    }
}
