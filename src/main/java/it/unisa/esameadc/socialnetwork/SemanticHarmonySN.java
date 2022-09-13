package it.unisa.esameadc.socialnetwork;


import java.util.List;

import net.tomp2p.peers.PeerAddress;

public interface SemanticHarmonySN {

	/**
	 * Gets the social network users questions.
	 * @return a list of String that is the profile questions.
	 */
	public List<String> getUserProfileQuestions();
	
	/**
	 * Creates a new user profile key according the user answers.
	 * @param _answer a list of answers.
	 * @return a String, the obtained profile key.
	 */
	public String createAuserProfileKey(List<Integer> _answer);
	
	/**
	 * Joins in the Network. An automatic messages to each potential new friend is generated.
	 * @param _profile_key a String, the user profile key according the user answers.
	 * @param _nick_name a String, the nickname of the user in the network.
	 * @return true if the join success, fail otherwise.
	 */
	public boolean join(String _profile_key,String _nick_name);
	
	/**
	 * Gets the nicknames of all automatically creates friendships. 
	 * @return a list of String.
	 */
	public List<String> getFriends();
	
	/**
	 * Create a profile_key topic if it doesn't exist yet.
	 * @param _profile_key a String, the user profile key according the user answers.
	 * @return true if the topic creation success, fail otherwise.
	 */
	public boolean createProfileTopic(String _profile_key);
	
	/**
	 * Subscribe the user to the profile_key topic.
	 * @param _profile_key a String, the user profile key according the user answers.
	 * @return true if the subscribe success, fail otherwise.
	 */
	public boolean subscribetoProfileTopic(String _profile_key);
	
	/**
	 * Send a message to all users subscribed to the same profile_key.
	 * @param _profile_key a String, the user profile key according the user answers.
	 * @param _obj the content of the sent message.
	 * @return true if the broadcast success, fail otherwise.
	 */
	public boolean broadcastMessage(String _profile_key, Object _obj);
	
	/**
	 * Send a message to a specific user.
	 * @param _profile_key a String, the user profile key according the user answers.
	 * @param _sender The recipient's peer address.
	 * @param _obj the content of the sent message.
	 * @return true if the sending success, fail otherwise.
	 */
	public  boolean directMessage(String _profile_key, PeerAddress _sender, Object _obj);
	
	/**
	 * Unsubscribe the user from profile_key topic so he can take the test again.
	 * @param _nick_name a String, the nickname of the user in the network.
	 * @param _profile_key a String, the name of the profile_key topic.
	 * @return true if the unsubscribe success, fail otherwise.
	 */
	public boolean unsubscribeProfileTopic(String _nick_name, String _profile_key);
	
	/**
	 * The user leaves the network.
	 * @return true if the leave success, fail otherwise.
	 */
	public boolean leaveNetwork();
	
}
