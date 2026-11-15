# Workspace

This is the independent Minecraft 1.20.1 Forge workspace on branch `forge/1.20.1`.

- Keep source, resources, build files, and dependencies local to this workspace.
- Preserve feature parity with the Minecraft 1.21.1 NeoForge main branch.
- Use Java 17. Build with `./gradlew build`; launch development with `./gradlew runClient`.
- Integrate JEI through Maven. Use KubeJS 6 for this Forge version.
- Keep regression tests in `src/test`; do not add `src/qa`.
- Do not add source comments or explanatory game UI text.
