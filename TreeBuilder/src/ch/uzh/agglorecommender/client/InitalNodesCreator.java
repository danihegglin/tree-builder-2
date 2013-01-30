package ch.uzh.agglorecommender.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.uzh.agglorecommender.clusterer.treecomponent.ENodeType;
import ch.uzh.agglorecommender.clusterer.treecomponent.IAttribute;
import ch.uzh.agglorecommender.clusterer.treecomponent.INode;
import ch.uzh.agglorecommender.clusterer.treecomponent.TreeComponentFactory;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * Parameter object that represents a passed data set in format processible
 * by the cluster and recommendation engines.
 *
 */
public class InitalNodesCreator {

	/**
	 * Map of user id as in data set to the corresponding node.
	 */
	private final ImmutableMap<Integer, INode> userLeavesMap;
	
	/**
	 * Map of content id as in data set to the corresponding node.
	 */
	private final ImmutableMap<Integer, INode> contentLeavesMap;
	
	/**
	 * Instantiates a new InitialNodesCreator object
	 * and creates the leaf nodes of the passed data set.
	 * This object is needed to start a new clustering process or
	 * as the input to the recommendation generation engine.
	 * 
	 * @param dataset the data set which shall be clustered (training) 
	 * or for which recommendations shall be generated (test).
	 * 
	 * @param contentTreeComponentFactory the initially specified factory
	 * @param userTreeComponentFactory the initially specified factory
	 */
	public InitalNodesCreator(IDataset<?> dataset,
			TreeComponentFactory contentTreeComponentFactory,
			TreeComponentFactory userTreeComponentFactory) {
		
		Map<Integer, List<IDatasetItem<?>>> usersMap = new HashMap<Integer, List<IDatasetItem<?>>>();
		Map<Integer, List<IDatasetItem<?>>> contentsMap = new HashMap<Integer, List<IDatasetItem<?>>>();
		
		// sort data items according to user id and content id
		Iterator<?> it = dataset.iterateOverDatasetItems();
		while(it.hasNext()) {
			IDatasetItem<?> datasetItem = (IDatasetItem<?>) it.next();
			if (usersMap.containsKey(datasetItem.getUserId())) {
				usersMap.get(datasetItem.getUserId()).add(datasetItem);
			} else {
				List<IDatasetItem<?>> li = new ArrayList<IDatasetItem<?>>();
				li.add(datasetItem);
				usersMap.put(datasetItem.getUserId(), li);
			}
			if (contentsMap.containsKey(datasetItem.getContentId())) {
				contentsMap.get(datasetItem.getContentId()).add(datasetItem);
			} else {
				List<IDatasetItem<?>> li = new ArrayList<IDatasetItem<?>>();
				li.add(datasetItem);
				contentsMap.put(datasetItem.getContentId(), li);
			}
		}
		
		// create for each user and content id one node
		Map<Integer, INode> usersNodeMap = new HashMap<Integer, INode>();
		for (Integer i : usersMap.keySet()) {
			usersNodeMap.put(i, userTreeComponentFactory.createLeafNode(ENodeType.User, i));
		}		
		Map<Integer, INode> contentsNodeMap = new HashMap<Integer, INode>();
		for (Integer i : contentsMap.keySet()) {
			contentsNodeMap.put(i, contentTreeComponentFactory.createLeafNode(ENodeType.Content, i));
		}
		
		// attach to each node its attributes map
		for (Map.Entry<Integer, List<IDatasetItem<?>>> entry : usersMap.entrySet()) {
			Map<INode, IAttribute> attributes = new HashMap<INode, IAttribute>();
			for (IDatasetItem<?> di : entry.getValue()) {
				double normalizedRating = ((INormalizer<Number>) dataset.getNormalizer()).normalizeRating( di.getValue());
				attributes.put(contentsNodeMap.get(di.getContentId()), contentTreeComponentFactory.createAttribute(normalizedRating));
			}
			usersNodeMap.get(entry.getKey()).setAttributes(attributes);
//			userNodes.add(usersNodeMap.get(entry.getKey()));
		}
		for (Map.Entry<Integer, List<IDatasetItem<?>>> entry : contentsMap.entrySet()) {
			Map<INode, IAttribute> attributes = new HashMap<INode, IAttribute>();
			for (IDatasetItem<?> di : entry.getValue()) {			
				double normalizedRating = ((INormalizer<Number>) dataset.getNormalizer()).normalizeRating( di.getValue());
				attributes.put(usersNodeMap.get(di.getUserId()), userTreeComponentFactory.createAttribute(normalizedRating));
			}
			contentsNodeMap.get(entry.getKey()).setAttributes(attributes);
//			contentNodes.add(contentsNodeMap.get(entry.getKey()));
		}
		
		userLeavesMap = ImmutableMap.copyOf(usersNodeMap);
		contentLeavesMap = ImmutableMap.copyOf(contentsNodeMap);
	}
	
	/**
	 * Gets the data set user id to leaf node map.
	 * 
	 * @return the immutable map of the user id as in the data set to the
	 * node in the cluster tree.
	 */
	public ImmutableMap<Integer, INode> getUserLeaves() {
		return userLeavesMap;
	}

	/**
	 * Gets the data set content id to leaf node map.
	 * 
	 * @return the immutable map of the content id as in the data set to the
	 * node in the cluster tree.
	 */
	public ImmutableMap<Integer, INode> getContentLeaves() {
		return contentLeavesMap;
	}
}