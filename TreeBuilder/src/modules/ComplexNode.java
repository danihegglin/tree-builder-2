package modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import clusterer.ENodeType;
import clusterer.IAttribute;
import clusterer.INode;
import clusterer.INodeDistance;
import clusterer.INodeDistanceCalculator;
import clusterer.IPrintableNode;
import clusterer.NodeIdComparator;


public class ComplexNode implements INode, IPrintableNode, Comparable<ComplexNode>{
	
	
	/**
	 * The unique id of this node.
	 */
	private final long id = ENodeType.getNewId();
	
	/**
	 * The node type.
	 */
	private final ENodeType nodeType;
	
	/**
	 * The attributes of the node.
	 * Stores for each node that is an
	 * attribute of this node a mapping
	 * to an IAttribute object.
	 */
	private Map<INode, IAttribute> attributes;
		
	/**
	 * The children of this node.
	 */
	private Set<INode> children = new HashSet<INode>();
	
	/**
	 * The parent of this node.
	 * Is null s long as the node was not merged.
	 */
	private INode parent = null;
	
	/**
	 * The distance calculator of the node.
	 */
	private INodeDistanceCalculator distanceCalculator;
	
	/**
	 * The distance calculator of the node.
	 */
	private ArrayList<Set> attributeGroups;

	public ComplexNode( ENodeType nodeType, INodeDistanceCalculator ndc) {
		this.distanceCalculator = ndc;
		this.nodeType = nodeType;
	}
	
	public ComplexNode(ENodeType nodeType, INodeDistanceCalculator ndc, Set<INode> children, Map<INode, IAttribute> attributes, ArrayList<INode> attributeGroup) {
		this.distanceCalculator = ndc;
		this.nodeType = nodeType;
		this.children = children;
		this.attributes = attributes;
		this.attributeGroups = attributeGroups;
	}
	
	@Override
	public double getDistance(INode otherNode) {
		return distanceCalculator.calculateDistance(this, otherNode);
	}
	
	@Override
	public INodeDistance getDistanceToClosestNode(List<INode> list) {
		double shortest = Double.MAX_VALUE;
		INode close = null;
		for (INode node : list) {
			if (node.equals(this)) continue;
			double tmp = this.getDistance(node);
			if (tmp < shortest) {
				shortest = tmp;
				close = node;
			}
		}
		return new SimpleNodeDistance(shortest, this, close);
	}
	
	@Override
	public String getAttributesString() {
		
		List<ComplexNode> keyList = new ArrayList<ComplexNode>((Collection<? extends ComplexNode>) attributes.keySet());
		Collections.sort(keyList, new NodeIdComparator());
		String s = "";
		for (IPrintableNode node : keyList) {
			s = s.concat(node.toString()).concat(": ").concat(attributes.get(node).toString()).concat(";\t");
		}

		if (s.length() == 0) {
			return "no_attributes";
		} else {
			return s.substring(0, s.length()-1);
		}
	}
		
	@Override
	public int compareTo(ComplexNode o) {
		return ((Long)this.getId()).compareTo((Long)o.getId());
	}
	
	@Override
	public Iterator<INode> getChildren() {
		return children.iterator();
	}
		
	@Override
	public boolean isChild(INode possibleChild) {
		return children.contains(possibleChild);
	}
	
	@Override
	public INode getParent() {
		return parent;
	}
	
	@Override
	public void addChild(INode child) {
		this.children.add(child);
	}
	
	@Override
	public INode setParent(INode parent) {
		INode prevP = this.parent;
		this.parent = parent;
		return prevP;
	}
	
	@Override
	public boolean isLeaf() {
		if (this.children.isEmpty()) return true;
		return false;
	}
	
	@Override
	public boolean isRoot() {
		if (this.parent == null) return true;
		return false;
	}
	
	@Override
	public void setAttributes(Map<INode, IAttribute> movies) {
		this.attributes = movies;
	}
	
	@Override
	public Set<INode> getAttributeKeys() {
		return attributes.keySet();
	}
	
	@Override
	public IAttribute getAttributeValue(INode node) {
		return attributes.get(node);
	}
				
	@Override
	public String toString() {
		return "SimpleNode".concat(" ").concat(String.valueOf(id));
	}

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public void addAttribute(INode node, IAttribute attribute) {
		attributes.put(node, attribute);		
	}

	@Override
	public ENodeType getNodeType() {
		return nodeType;
	}
	
	// ------------------------------------------------------------------------> Neu
	public ArrayList<Set> getAttributeGroups() {
		return attributeGroups;
	}

	@Override
	public boolean hasAttribute(INode attribute) {
		if(attributes.containsKey(attribute)) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void removeAttribute(INode attribute) {
		attributes.remove(attribute);
	}

	@Override
	public void addAttributeGroup(Set<INode> attributeGroup) {
		attributeGroups.add(attributeGroup);
	}
	
}