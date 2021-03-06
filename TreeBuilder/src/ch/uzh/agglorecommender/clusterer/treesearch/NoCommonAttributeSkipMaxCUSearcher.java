package ch.uzh.agglorecommender.clusterer.treesearch;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import ch.uzh.agglorecommender.clusterer.treecomponent.INode;
import ch.uzh.agglorecommender.util.TBLogger;

import com.google.common.collect.Sets;

public class NoCommonAttributeSkipMaxCUSearcher extends MaxCategoryUtilitySearcherDecorator implements Serializable {

	/**
	 * Determines if a de-serialized file is compatible with this class.
	 * <br>
	 * <br>
	 * Maintainers must change this value if and only if the new version
	 * of this class is not compatible with old versions.
	 */
	private static final long serialVersionUID = 1L;
	
	Set<Collection<INode>> combinationsWithSharedAttributes = new HashSet<Collection<INode>>();
	
	/**
	 *  Keeps track of combinations which have been identified as combinations with shared nodes.
	 *  These combinations won't be tested on shared attributes in a subsequent clustering cycle again.
	 *  This means that combination id's contained in this set will never be removed from the 
	 *  set of combinations passed to @code{getMaxCategoryUtilityMerges} to calculate its
	 *  category utility by this decorator. 
	 */
	TIntSet combinationsIndicesWithSharedAttributes = new TIntHashSet();
	
	public NoCommonAttributeSkipMaxCUSearcher(IMaxCategoryUtilitySearcher decoratedSearcher) {
		super(decoratedSearcher);
	}

	@Override
	public Set<IMergeResult> getMaxCategoryUtilityMerges(
			Set<Collection<INode>> combinationsToCheck, IClusterSet<INode> clusterSet) {
		Logger log = TBLogger.getLogger(getClass().getName());
		long time = System.nanoTime();
		int removedLists = 0;
		int initCombinationsSize = combinationsToCheck.size();
		Iterator<Collection<INode>> i = combinationsToCheck.iterator();
		while (i.hasNext()) {
			Collection<INode> l =  i.next();
			if (combinationsWithSharedAttributes.contains(l)) continue;
			if (l.size() != 2) continue;
			
			Iterator<INode> it = l.iterator();
			INode first = it.next();
			INode second = it.next();
			Set<INode> intersection = Sets.intersection(first.getAttributeKeys(), second.getAttributeKeys());
			if (intersection.size() == 0) {
				removedLists++;
				i.remove();
			} else {
				combinationsWithSharedAttributes.add(l);
			}		
		}
		log.info("Time in NoCommonAttributeSkipDecorator: "
				+ (double)(System.nanoTime() - time) / 1000000000.0 + " seconds, "
				+ "Number of removed comparisons: " + removedLists + " of " + initCombinationsSize);

		return decoratedSearcher.getMaxCategoryUtilityMerges(combinationsToCheck, clusterSet);
	}

	@Override
	public TIntDoubleMap getMaxCategoryUtilityMerges(
			TIntSet combinationIds, IClusterSetIndexed<INode> clusterSet) {
		Logger log = TBLogger.getLogger(getClass().getName());
		long time = System.nanoTime();
		
		// initialize performance indicators
		int removedLists = 0;
		int initCombinationsSize = combinationIds.size();
		
		// iterate over the collection of possible combinations
		TIntIterator iterator = combinationIds.iterator();
		for ( int i = combinationIds.size(); i-- > 0; ) {  // faster iteration by avoiding hasNext()
			int combination = iterator.next();
			
			// skip this combination if nodes are known to have shared attributes
			if (combinationsIndicesWithSharedAttributes.contains(combination)) continue;
			
			// get the attributes of both nodes of the combination
			Iterator<INode> it = clusterSet.getCombination(combination).iterator();
			Set<INode> attFirst = it.next().getAttributeKeys();
			Set<INode> attSecond = it.next().getAttributeKeys();
			
			// check the nodes of the combinations share an attribute
			boolean remove = true;
			for (INode aF : attFirst) {
				if (attSecond.contains(aF)) {
					// don't remove combination if nodes share an attribute
					remove = false;
					break;
				}			
			}
			 
			if (remove) {
				// remove combinations without shared attributes from the collection of possible merges
				removedLists++;
				iterator.remove();
			} else {
				// store the combination with a shared attribute for future speed up of this filter
				combinationsIndicesWithSharedAttributes.add(combination);
			}		
		}
		log.info("Time in NoCommonAttributeSkipDecorator: "
				+ (double)(System.nanoTime() - time) / 1000000000.0 + " seconds, "
				+ "Number of removed comparisons: " + removedLists + " of " + initCombinationsSize);
		return decoratedSearcher.getMaxCategoryUtilityMerges(combinationIds, clusterSet);
	}
	
}
