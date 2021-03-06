package ch.uzh.agglorecommender.clusterer.treecomponent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import ch.uzh.agglorecommender.clusterer.treesearch.CobwebMaxCategoryUtilitySearcher;

import com.google.common.collect.ImmutableMap;

public class CobwebTreeComponentFactory extends TreeComponentFactory implements Serializable {

	/**
	 * Determines if a de-serialized file is compatible with this class.
	 * <br>
	 * <br>
	 * Maintainers must change this value if and only if the new version
	 * of this class is not compatible with old versions.
	 */
	private static final long serialVersionUID = 1L;
	
	private static CobwebTreeComponentFactory factory = new CobwebTreeComponentFactory();
	
	/*
	 * Must not be instantiated with constructor.
	 */
	private CobwebTreeComponentFactory() {
		// singleton
	}
	
	public static  TreeComponentFactory getInstance() {
		return factory;
	}
	
	@Override
	public IAttribute createNumericAttribute(double rating) {
		Map<Double, Double> attMap = ImmutableMap.of(rating, 1.0);
		return new CobwebAttribute(attMap);
	}

	@Override
	public IAttribute createNominalAttribute(int support, Object key, Object object) {
		Map<?, Double> attMap = ImmutableMap.of(key, 1.0);
		return new CobwebAttribute(attMap);
	}

	@Override
	public IAttribute createMergedAttribute(Object object, Collection<INode> nodesToMerge) {
		int totalLeafCount = 0;
		for (INode node : nodesToMerge) {
			totalLeafCount += node.getNumberOfLeafNodes();
		}
		Map<Object, Double> attMap = ImmutableMap.copyOf(
				CobwebMaxCategoryUtilitySearcher
					.calculateAttributeProbabilities(
							(INode)object, nodesToMerge, totalLeafCount
					)
				);
		
		return new CobwebAttribute(attMap);
	}
}
