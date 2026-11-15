# Workspace

This is the independent Minecraft 26.1.1 NeoForge workspace on branch `neoforge/26.1.1`.

- Keep source, resources, build files, and dependencies local to this workspace.
- Preserve feature parity with the Minecraft 1.21.1 NeoForge main branch.
- Use Java 25. Build with `./gradlew build`; launch development with `./gradlew runClient`.
- Integrate JEI through Maven. Keep the KubeJS 8 API optional and respect upstream Minecraft version availability.
- Keep regression tests in `src/test`; do not add `src/qa`.
- Do not add source comments or explanatory game UI text.
