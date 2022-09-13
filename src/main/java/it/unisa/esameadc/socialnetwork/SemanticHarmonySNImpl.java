package it.unisa.esameadc.socialnetwork;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class SemanticHarmonySNImpl implements SemanticHarmonySN {
	final private Peer peer;
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT = 4000;
	
	public static List<String> friendsList = new ArrayList<String>();
	public static String newUser;
	public static List<PeerAddress> friendRecipientsList = new ArrayList<PeerAddress>();
	public static PeerAddress sender;

	final private ArrayList<String> s_topics = new ArrayList<String>();

	public SemanticHarmonySNImpl(int _id, String _master_peer, final MessageListener _listener) throws Exception {
		peer = new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT + _id).start();
		_dht = new PeerBuilderDHT(peer).start();

		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer))
				.ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if (fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		} else {
			throw new Exception("Error in master peer bootstrap.");
		}

		peer.objectDataReply(new ObjectDataReply() {

			public Object reply(PeerAddress _sender, Object _request) throws Exception {
				String _strRequest = _request.toString();

				if (_strRequest.contains("*FRIEND*")) {
					newUser = _strRequest;
					sender = _sender;
				}
				return _listener.parseMessage(_request);
			}
		});
	}

	
	public List<String> getUserProfileQuestions() {
		List<String> questions = new ArrayList<String>();
		questions = Arrays.asList(
				"\nYou’re in trouble when you are with other people?\n" + "1. I strongly agree\n"
						+ "2. I strongly disagree\n" + "3. I agree\n" + "4. I disagree\n\n",
				"\nDuring a conversation...\n" + "1. You don't listen what they say and your imagination fly\n"
						+ "2. You hardly agree with others because you’re always right\n"
						+ "3. You always agree with others even if they don't agree with what they say\n"
						+ "4. You open to dialogue even if you don’t agree with some of the arguments\n\n",
				"\nYou're often jealous?\n" + "1. I fully agree, my life sucks\n"
						+ "2. Others should be the ones who are jealous of me\n" + "3. I fully agree\n"
						+ "4. Why should I be jealous?\n\n",
				"\nIf your friend  doesn't answer to your message quickly...\n" + "1. I probably said something wrong\n"
						+ "2. Sooner or later he will answer\n"
						+ "3. I'll send you another one that hasn't arrived yet\n"
						+ "4. If it's something important, I'll call him back later\n\n",
				"\nDuring a party...\n"
						+ "1. I isolate myself and take a look at the phone so as not to catch the eye\n"
						+ "2. I go wild and start the dances\n" + "3. I stay in my circle of friends\n"
						+ "4. Trying to make new friends\n\n");
		return questions;
	}
	
	public String createAuserProfileKey(List<Integer> _answer) {
		int size = _answer.size();
		int score = 0;
		String profileKey = "unclassified";

		for (int i = 0; i < size; i++) {
			score = score + _answer.get(i);
		}

		if ((score >= 5) && (score < 10)) {
			profileKey = "shy";
		} else if ((score >= 10) && (score < 15)) {
			profileKey = "introvert";
		} else if ((score >= 15) && (score < 20)) {
			profileKey = "friendly";
		} else if ((score >= 20) && (score < 25)) {
			profileKey = "extrovert";
		}

		return profileKey;
	}
	
	
	public boolean join(String _profile_key, String _nick_name) {
		if (subscribetoProfileTopic(_profile_key)) {
			// send notify to friends
			if (broadcastMessage(_profile_key, "[*FRIEND*]: " + _nick_name)) {
				return true;
			}
			return true;

		} else if (createProfileTopic(_profile_key)) {
			if (subscribetoProfileTopic(_profile_key)) {
				if (broadcastMessage(_profile_key,
						"[*NOTIFY*]: " + _nick_name + " created " + _profile_key + " group.\n")) {
					return true;
				}
			}
			return true;
		} else
			return false;
	}
	
	public List<String> getFriends() {
		List<String> _temp = friendsList;
		List<String> _friendslist = new ArrayList<String>();

		int size = _temp.size();
		for (int i = 0; i < size; i++) {
			int startIndex = _temp.get(i).indexOf("[");
			int endIndex = _temp.get(i).indexOf("]");
			String replacement = Integer.toString(i+1);
			String toBeReplaced = _temp.get(i).substring(startIndex + 1, endIndex);
			_friendslist.add(_temp.get(i).replace(toBeReplaced, replacement));
		}
		
		return _friendslist;
	}
	
	public boolean createProfileTopic(String _profile_key) {

		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_profile_key)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess() && futureGet.isEmpty())
				_dht.put(Number160.createHash(_profile_key)).data(new Data(new HashSet<PeerAddress>())).start()
						.awaitUninterruptibly();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean subscribetoProfileTopic(String _profile_key) {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_profile_key)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if (futureGet.isEmpty())
					return false;
				HashSet<PeerAddress> peers_on_topic;
				peers_on_topic = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				peers_on_topic.add(_dht.peer().peerAddress());
				_dht.put(Number160.createHash(_profile_key)).data(new Data(peers_on_topic)).start()
						.awaitUninterruptibly();
				s_topics.add(_profile_key);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean broadcastMessage(String _profile_key, Object _obj) {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_profile_key)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				HashSet<PeerAddress> peers_on_topic;
				peers_on_topic = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				for (PeerAddress peer : peers_on_topic) {
					FutureDirect futureDirect = _dht.peer().sendDirect(peer).object(_obj).start(); 
					futureDirect.awaitUninterruptibly();
				}

				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean directMessage(String _profile_key, PeerAddress _sender, Object _obj) {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_profile_key)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				FutureDirect futureDirect = _dht.peer().sendDirect(_sender).object(_obj).start();
				futureDirect.awaitUninterruptibly();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean unsubscribeProfileTopic(String _nick_name, String _profile_key) {
		try {
			FutureGet futureGet = _dht.get(Number160.createHash(_profile_key)).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) {
				if (futureGet.isEmpty())
					return false;
				if (_nick_name != null) {
					if (broadcastMessage(_profile_key,
							"\n[*NOTIFY*]: " + _nick_name + " may change his profile because he'll take the test again\n")) {
					}
				}
				HashSet<PeerAddress> peers_on_topic;
				peers_on_topic = (HashSet<PeerAddress>) futureGet.dataMap().values().iterator().next().object();
				peers_on_topic.remove(_dht.peer().peerAddress());
				_dht.put(Number160.createHash(_profile_key)).data(new Data(peers_on_topic)).start()
						.awaitUninterruptibly();
				s_topics.remove(_profile_key);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean leaveNetwork() {
		for (String topic : new ArrayList<String>(s_topics))
			unsubscribeProfileTopic(null, topic);
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		return true;
	}

}
