package bridgedesigner;

/**
 * Created by mehnadnerd on 2016-02-09.
 */
public class RunLoadTest {
    public static boolean doLoadTest(EditableBridgeModel bridge) {
        // Fixup might be needed due certain joint move edge cases.
        FixupCommand ruleEnforcer = new FixupCommand(bridge);
        int revisedMemberCount = ruleEnforcer.revisedMemberCount();
        if (revisedMemberCount > 0) {
            ruleEnforcer.execute(bridge.getUndoManager());

        }
        // Analyze the bridge the first time.
        bridge.analyze();

        return bridge.isPassing();
    }
}
