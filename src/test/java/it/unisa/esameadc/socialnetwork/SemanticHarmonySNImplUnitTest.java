package it.unisa.esameadc.socialnetwork;

import java.util.ArrayList;
import java.util.List;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.junit.Test;

import junit.framework.TestCase;

public class SemanticHarmonySNImplUnitTest extends TestCase {
	private SemanticHarmonySNImpl peer0, peer1, peer2, peer3;

	// Activate options 7 and 8 only when it arrives a new [*FRIEND*] message
	private static boolean newFriendChoice = false;

	// Keep the user's nickname
	private static String nickname = "test-user";

	String profileKey = "unclassified";

	class MessageListenerImpl implements MessageListener {
		int peerid;

		public MessageListenerImpl(int peerid) {
			this.peerid = peerid;
		}

		public Object parseMessage(Object obj) {

			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			String message = obj + "";
			if ((message.contains("*FRIEND*")) && (!message.contains(nickname))) {
				newFriendChoice = true;
				message = "\n" + obj
						+ " could be your friend. Do you want to add it to your list?\n\n7 - YES\n\n8 - NO\n\nOption: ";
			} else if ((message.contains("*FRIEND*")) && (message.contains(nickname))) {
				message = "";
			}
			System.out.println(message + "\n");

			return "success";
		}
	}

	@Test
	public void testMethods() throws Exception {
		peer0 = new SemanticHarmonySNImpl(0, "127.0.0.1", new MessageListenerImpl(0));
		peer1 = new SemanticHarmonySNImpl(1, "127.0.0.1", new MessageListenerImpl(1));
		peer2 = new SemanticHarmonySNImpl(2, "127.0.0.1", new MessageListenerImpl(2));
		peer3 = new SemanticHarmonySNImpl(3, "127.0.0.1", new MessageListenerImpl(3));
		
		int size = peer0.getUserProfileQuestions().size();
		List<Integer> answer = new ArrayList<Integer>();
		
		for (int i = 0; i < size; i++) {
			answer.add(4);
		}
		profileKey = peer0.createAuserProfileKey(answer);
		assertSame(profileKey, "extrovert");
		
		nickname = "Alice";
		System.out.println("ALICE TAKES THE TEST AND CREATES EXTROVERT GROUP\n");
		assertTrue(peer0.join(profileKey, "Alice"));

		
		nickname = "Bob";
		System.out.println("BOB TAKES THE TEST AND PARTICIPATES IN THE EXTROVERTED GROUP\n");
		assertTrue(peer1.join(profileKey, "Bob"));
		
		System.out.println("ALICE ADDS BOB TO HER FRIENDS LIST\n");
		assertNotNull(SemanticHarmonySNImpl.friendsList.add(SemanticHarmonySNImpl.newUser));
		
		System.out.println("BOB SENDS A DIRECT MESSAGE TO ALICE\n");
		assertTrue(peer1.directMessage(profileKey, SemanticHarmonySNImpl.sender, "\n[DM:Bob]: " + "I love you, Alice"));
		
		System.out.println("ALICE RESETS HER PROFILE\n");
		assertTrue(peer0.unsubscribeProfileTopic("Alice", profileKey));
		
		size = peer0.getUserProfileQuestions().size();
		answer = new ArrayList<Integer>();
		
		for (int i = 0; i < size; i++) {
			answer.add(1);
		}
		profileKey = peer0.createAuserProfileKey(answer);
		assertSame(profileKey, "shy");
		
		System.out.println("ALICE REPEATS THE TEST AND CREATES SHY GROUP\n");
		nickname = "Alice";
		assertTrue(peer0.join(profileKey, nickname));
		
		System.out.println("CARL TAKES THE TEST AND PARTICIPATES IN THE SHY GROUP\n");
		nickname = "Carl";
		assertTrue(peer2.join(profileKey, nickname));
		System.out.println("ALICE ADDS CARL TO HER FRIENDS LIST\n");
		assertNotNull(SemanticHarmonySNImpl.friendsList.add(SemanticHarmonySNImpl.newUser));
		
		System.out.println("FRANK TAKES THE TEST AND PARTICIPATES IN THE SHY GROUP\n");
		nickname = "Frank";
		assertTrue(peer3.join(profileKey, nickname));
		System.out.println("ALICE ADDS FRANK TO HER FRIENDS LIST\n");
		assertNotNull(SemanticHarmonySNImpl.friendsList.add(SemanticHarmonySNImpl.newUser));
		
		
		System.out.println("ALICE SEND A MESSAGE IN THE SEMANTIC HARMONY ROOM\n");
		assertTrue(peer0.broadcastMessage(profileKey, "\n[Alice]: Hi guys\n"));
		System.out.println("CARL AND FRANK REPLY MESSAGE\n");
		assertTrue(peer2.broadcastMessage(profileKey, "\n[Carl]: Hi Alice\n"));
		assertTrue(peer3.broadcastMessage(profileKey, "\n[Frank]: Hi\n"));
		
		System.out.println("ALICE VIEWS HER FRIENDS LIST\n");
		int sizeFriends = peer0.getFriends().size();
		if (sizeFriends > 0) {
			for (int i = 0; i < sizeFriends; i++) {
				System.out.println(peer0.getFriends().get(i) + "\n");
			}
		}
		
		System.out.println("Alice leaves the network");
		assertTrue(peer0.leaveNetwork());
	}
}
