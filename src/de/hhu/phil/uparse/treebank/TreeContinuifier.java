package de.hhu.phil.uparse.treebank;

import java.util.HashMap;
import java.util.List;

import de.hhu.phil.uparse.treebank.Tree.GapType;

public class TreeContinuifier implements TreeProcessor<Tree> {
	
	final public static int threshold = 0;
	
	// left, right, label, dist, random
	public String mode;
	
	public HashMap<String,String> labels;
	
	public List<Tree> reordered;
	
	public TreeContinuifier(String mode, HashMap<String,String> labels) {
		this.mode = mode;
		this.labels = labels;
	}
	
	public TreeContinuifier(String mode) {
		this(mode, null);
	}
	
	public List<Tree> reorder(Tree tree) throws TreebankException {
		List<Tree> result = null;
		List<Tree> children = tree.children();
		if (children.size() > 2) {
			throw new TreebankException("tree must be binarized");
		}
		if (children.size() == 1 || tree.isPreTerminal()) {
			return tree.preTerminals();
		}
		if (children.size() == 2) {
			Tree left = children.get(0);
			Tree right = children.get(1);
			if ("left".equals(mode)) {
				result = reorder(left);
				result.addAll(reorder(right));
			} else if ("right".equals(mode)) {
				result = reorder(right);
				result.addAll(reorder(left));
			} else if ("rightd".equals(mode)) {
				if (tree.gapType().equals(GapType.SOURCE)) {
					result = reorder(right);
					result.addAll(reorder(left));
				} else {
					result = reorder(left);
					result.addAll(reorder(right));
				}
			} else if ("label".equals(mode)) {
				String direction = "left";
				String label = tree.getLabel().label;
				if (labels.containsKey(tree.getLabel().label)) {
					direction = labels.get(label);
				}
				if ("left".equals(direction)) {
					result = reorder(left);
					result.addAll(reorder(right));
				} else if ("right".equals(direction)) {
					result = reorder(right);
					result.addAll(reorder(left));
				}
			} else if ("dist".equals(mode)) {
				int leftGap = left.gapLengthSum();
				int rightGap = right.gapLengthSum();
				if (tree.gapType().equals(GapType.SOURCE) && (rightGap > threshold || leftGap > threshold)) {
					result = reorder(right);
					result.addAll(reorder(left));
				} else {
					result = reorder(left);
					result.addAll(reorder(right));
				}
			} else if ("random".equals(mode)) {
				boolean decision = true;
				if (Math.random() < 0.5) {
					decision = false;
				}
				if (decision) {
					result = reorder(right);
					result.addAll(reorder(left));
				} else {
					result = reorder(left);
					result.addAll(reorder(right));
				}
			}
		}
		return result;
	}
	
	@Override
	public void process(Tree tree) throws TreebankException {
		reordered = reorder(tree);
	}

}
