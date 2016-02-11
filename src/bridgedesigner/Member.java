package bridgedesigner;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Implements members for bridge models.
 * 
 * @author Eugene K. Ressler
 */
public class Member {

    private int index = -1;
    private Joint jointA;
    private Joint jointB;
    private Material material;
    private Shape shape;
    private boolean selected = false;
    private double compressionForceStrengthRatio = -1;
    private double tensionForceStrengthRatio = -1;


    /** 
     * These tables relate height and width of a parabola with unit arc length. 
     * We interpolate linearly below to get height values for compression failures.
     */
    private static final float [] parabolaWidth =
        {0.000000f,0.062500f,0.125000f,0.156250f,
         0.187500f,0.218750f,0.250000f,0.281250f,
         0.312500f,0.343750f,0.375000f,0.406250f,
         0.437500f,0.468750f,0.500000f,0.531250f,
         0.562500f,0.593750f,0.625000f,0.656250f,
         0.687500f,0.718750f,0.750000f,0.781250f,
         0.812500f,0.843750f,0.875000f,0.906250f,
         0.937500f,0.968750f,0.984375f,0.992188f,
         1.000000f,};
    private static final float [] parabolaHeight =
        {0.500000f,0.497717f,0.492161f,0.488378f,
         0.483979f,0.478991f,0.473431f,0.467310f,
         0.460633f,0.453402f,0.445614f,0.437259f,
         0.428326f,0.418797f,0.408650f,0.397857f,
         0.386383f,0.374182f,0.361201f,0.347371f,
         0.332606f,0.316796f,0.299798f,0.281420f,
         0.261396f,0.239344f,0.214672f,0.186382f,
         0.152526f,0.108068f,0.076484f,0.054105f,
         0.000000f,};

    /**
     * This tells us how high a parabola should result if a a member with getLength <code>arcLen</code>
     * buckles to a parabola with getLength <code>width</code>.
     * 
     * @param buckledLength buckled getLength of 
     * @param baseLength
     * @return height
     */
    public static float getParabolaHeight(float buckledLength, float baseLength) {
        if (baseLength == 0) {
            return 0f;
        }
        float p = Math.min(buckledLength / baseLength, 1);
        // Binary search in pTable.
        int i = Utility.getIndexOfGreatestNotGreaterThan(p, parabolaWidth);
        if (i == parabolaWidth.length - 1) {
            return 0f;
        }
        // Interpolate.
        float t = (p - parabolaWidth[i]) / (parabolaWidth[i + 1] - parabolaWidth[i]);
        float h = (i == parabolaWidth.length - 1) ? 0f : parabolaHeight[i] * (1f - t) + parabolaHeight[i + 1] * t;
        return h * baseLength;
    }

    /**
     * Construct a new member with undefined index and materials taken from tiven member
     * 
     * @param member member to copy materials from
     * @param a first joint of member
     * @param b second joint of member
     */
    public Member(Member member, Joint a, Joint b) {
        this(a, b, member.material, member.shape);
    }
    
    /**
     * Construct a new member with undefined index and given material and shape.
     * 
     * @param a first joint of member
     * @param b second joint of member
     * @param material material of member
     * @param shape shape of member
     */
    public Member(Joint a, Joint b, Material material, Shape shape) {
        jointA = a;
        jointB = b;
        this.material = material;
        this.shape = shape;
    }

    /**
     * Construct a new member taking everything from a different member except its material and shape.
     * 
     * @param member member from which index and joints of new member are copied
     * @param material material of the member
     * @param shape shape of the member
     */
    public Member(Member member, Material material, Shape shape) {
        this(member.index, member.jointA, member.jointB, material, shape);
    }

    /**
     * Construct a new member with given index, material, and shape.
     * 
     * @param index index of the member
     * @param a first joint of member
     * @param b second joint of member
     * @param material material of member
     * @param shape shape of member
     */
    public Member(int index, Joint a, Joint b, Material material, Shape shape) {
        this(a, b, material, shape);
        this.index = index;
    }

    /**
     * Construct a new member by copying index and joints from a given member and taking material and shape
     * from a given inventory unless the respective index is unspecified (has value -1).
     * 
     * @param member member from which to take index and joints
     * @param inventory inventory of construction stocks
     * @param materialIndex index of stock material in the inventory
     * @param sectionIndex index of stock  section in the inventory
     * @param sizeIndex index of stock size in the inventory
     */
    public Member(Member member, Inventory inventory, int materialIndex, int sectionIndex, int sizeIndex) {
        index = member.index;
        jointA = member.jointA;
        jointB = member.jointB;
        if (materialIndex == -1) {
            materialIndex = member.getMaterial().getIndex();
        }
        if (sectionIndex == -1) {
            sectionIndex = member.getShape().getSection().getIndex();
        }
        if (sizeIndex == -1) {
            sizeIndex = member.getShape().getSizeIndex();
        }
        material = inventory.getMaterial(materialIndex);
        shape = inventory.getShape(sectionIndex, sizeIndex);
    }

    /**
     * Construct a new member with everything taken from an existing member except its shape.
     * 
     * @param member member from which to copy index, joints, and material
     * @param shape
     */
    public Member(Member member, Shape shape) {
        this(member, member.material, shape);
    }

    /**
     * Get last-calculated ratio of compression force on the member to strength due to analysis.
     * 
     * @return ratio of compression force to member strength
     */
    public double getCompressionForceStrengthRatio() {
        return compressionForceStrengthRatio;
    }

    /**
     * Used by Analysis to set the compression force to strength ratio.
     * 
     * @param compressionForceStrengthRatio ratio of compression force to member strength
     */
    public void setCompressionForceStrengthRatio(double compressionForceStrengthRatio) {
        this.compressionForceStrengthRatio = compressionForceStrengthRatio;
    }

    /**
     * Get last-calculated ratio of tension force on the member to strength due to analysis.
     * 
     * @return ratio of tension force to member strength
     */
    public double getTensionForceStrengthRatio() {
        return tensionForceStrengthRatio;
    }

    /**
     * Used by Analysis to set the tension force to strength ratio.
     * 
     * @param tensionForceStrengthRatio ratio of tension force to member strength
     */
    public void setTensionForceStrengthRatio(double tensionForceStrengthRatio) {
        this.tensionForceStrengthRatio = tensionForceStrengthRatio;
    }

    /**
     * Swap the joints, material, and shape of this member with another one.
     * Used for command execute/undo/redo processing.
     * 
     * @param otherSelectable the other member with which to swap contents
     */
    public void swapContents(Editable otherSelectable) {
        Member other = (Member) otherSelectable;

        Joint tmpJoint = jointA;
        jointA = other.jointA;
        other.jointA = tmpJoint;

        tmpJoint = jointB;
        jointB = other.jointB;
        other.jointB = tmpJoint;

        Material tmpMaterial = material;
        material = other.material;
        other.material = tmpMaterial;

        Shape tmpShape = shape;
        shape = other.shape;
        other.shape = tmpShape;
   }

    /**
     * Set the material of this member.
     * 
     * @param material material value
     */
    public void setMaterial(Material material) {
        this.material = material;
    }

    /**
     * Get the material of this member.
     * 
     * @return material value
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Set the shape of this member.
     * 
     * @param shape shape value
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    /**
     * Get the shape of this member.
     * 
     * @return shape value
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Get the first joint of this member.
     * 
     * @return first joint
     */
    public Joint getJointA() {
        return jointA;
    }

    /**
     * Get the second joint of this member.
     * 
     * @return second joint.
     */
    public Joint getJointB() {
        return jointB;
    }

    /**
     * Get the number of this member. By convention, this is just one more than the index.
     * 
     * @return member number
     */
    public int getNumber() {
        return index + 1;
    }

    /**
     * Set the 0-based index of this member.
     * 
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the 0-based index of this member.
     * @return index of member
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the slenderness ratio for this member.
     * 
     * @return slenderness ratio
     */
    public double getSlenderness() {
        return getLength() * shape.getInverseRadiusOfGyration();
    }
    
    /**
     * Return an indicator of whether this member is selected or not.
     * 
     * @return true iff the member is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set the selected indicator for this member.
     * 
     * @param selected new value of indicator
     * @return true iff the set operation resulted in a change
     */
    public boolean setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            return true;
        }
        return false;
    }

    /**
     * Get the geometric getLength of this member.
     * 
     * @return Euclidean getLength
     */
    public double getLength() {
        return jointA.getPointWorld().distance(jointB.getPointWorld());
    }
    
    /**
     * Compute the pick distance from a point to a member where the joints have given world pixelRadius.
     * 
     * @param pt pick point
     * @param jointRadius world joint pixelRadius
     * @return pick distance to member
     */
    public double pickDistanceTo(Affine.Point pt, double jointRadius) {
        Affine.Point a = jointA.getPointWorld();
        Affine.Point b = jointB.getPointWorld();
        Affine.Vector d = b.minus(a);
        double len = d.length();
        // Do point pick if line is less than 2 joint diameters long.
        if (4 * jointRadius >= len) {
            return pt.distance(a.plus(d.times(0.5)));
        }
        // Else do a line segment pick.
        Affine.Vector v = d.unit(2 * jointRadius);
        return pt.distanceToSegment(a.plus(v), b.minus(v));
    }

    /**
     * Check whether the given joint is one of the joints of this member.
     * 
     * @param joint other joint
     * @return truee iff the given joint is one of this member's joints
     */
    public boolean hasJoint(Joint joint) {
        return (joint == jointA || joint == jointB);
    }

    /**
     * Check whether the given joints are the joints of this member.
     * 
     * @param jointA first joint
     * @param jointB second joint
     * @return true iff the given joints match this member
     */
    public boolean hasJoints(Joint jointA, Joint jointB) {
        return hasJoint(jointA) && hasJoint(jointB);
    }

    /**
     * If the given joint is one of this member's joints, return the other one.  Return null
     * iff the given joint does not belong to this member.
     * 
     * @param joint joint to check for
     * @return other member of joint or null if given joint does not belong to this member
     */
    public Joint otherJoint(Joint joint) {
        return joint == jointA ? jointB : joint == jointB ? jointA : null;
    }




    /**
     * Get the 3D drawing width of this member in meters. 
     * 
     * @return member width in meters
     */
    public float getWidthInMeters() {
        return (float)shape.getWidth() * 0.001f; // millimeter correction
    }

    /**
     * Get a string representation of this member.  Used for tip text.
     * 
     * @return string representation
     */
    @Override public String toString() {
        return "Member"+
                getNumber()+ shape.getName()+ material+ shape.getSection();
    }

    /**
     * No cursor for hot selectable interface.
     * 
     * @return null cursor
     */
    public Cursor getCursor() {
        return null;
    }
}
