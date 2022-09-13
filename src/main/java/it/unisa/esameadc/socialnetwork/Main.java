package it.unisa.esameadc.socialnetwork;

import java.util.List;
import java.util.ArrayList;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Main {

	@Option(name = "-m", aliases = "--masterip", usage = "the master peer ip address", required = true)
	private static String master;

	@Option(name = "-id", aliases = "--identifierpeer", usage = "the unique identifier for this peer", required = true)
	private static int id;

	//Activate options 7 and 8 only when it arrives a new [*FRIEND*] message
	private static boolean newFriendChoice = false;
	
	//Keep the user's nickname
	private static String nickname = null;

	public static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	/**
	 * Used only in Semantic Harmony Room, exits the user from chat if the user's message is equal to "*end".
	 * @param str1, str2 the strings to compare.
	 * @return 0 if the strings are the same, not 0 otherwise.
	 */
	public static int stringCompare(String str1, String str2) {

		int l1 = str1.length();
		int l2 = str2.length();
		int lmin = Math.min(l1, l2);

		for (int i = 0; i < lmin; i++) {
			int str1_ch = (int) str1.charAt(i);
			int str2_ch = (int) str2.charAt(i);

			if (str1_ch != str2_ch) {
				return str1_ch - str2_ch;
			}
		}

		if (l1 != l2) {
			return l1 - l2;
		}

		else {
			return 0;
		}
	}
	
	

	public static void main(String[] args) throws Exception {

		boolean isProfiled = false;
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
				terminal.printf(message + "\n");

				return "success";
			}

		}
		Main example = new Main();
		final CmdLineParser parser = new CmdLineParser(example);
		try {
			parser.parseArgument(args);
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			SemanticHarmonySNImpl peer = new SemanticHarmonySNImpl(id, master, new MessageListenerImpl(id));

			terminal.printf("\nStaring peer id: %d on master node: %s\n", id, master);
			while (true) {
				printMenu(terminal);
				int option = textIO.newIntInputReader().withMaxVal(8).withMinVal(1).read("Option");
				switch (option) {
				case 1:
					newFriendChoice = false;
					if (!isProfiled) {
						terminal.printf("\nCREATE YOUR PROFILE\n\n");

						int size = peer.getUserProfileQuestions().size();

						List<Integer> answer = new ArrayList<Integer>();

						if (nickname == null) {
							nickname = textIO.newStringInputReader().withDefaultValue("default-user")
									.read("First of all, choose your nickname ");
							terminal.printf("Well done " + nickname + ", now answer the following questions:\n\n");
						}
						for (int i = 0; i < size; i++) {
							terminal.printf(peer.getUserProfileQuestions().get(i));
							int choice = textIO.newIntInputReader().withMaxVal(4).withMinVal(1).read("Your choice");
							switch (choice) {
							case 1:
								answer.add(1);
								break;
							case 2:
								answer.add(4);
								break;
							case 3:
								answer.add(2);
								break;
							case 4:
								answer.add(3);
								break;
							default:
								break;
							}

						}

						profileKey = peer.createAuserProfileKey(answer);

						if (profileKey != "unclassified")
							terminal.printf("\nTest result: you are " + profileKey + "\n");
						if (peer.join(profileKey, nickname)) {
							terminal.printf("\nProfile created successfully\n");
							isProfiled = true;
						} else {
							terminal.printf("\nSomething went wrong. Please try later\n");
						}

					} else
						terminal.printf("You have already created your profile\n");

					break;
				case 2:
					newFriendChoice = false;
					if (isProfiled) {
						terminal.printf("\nYOUR FRIENDS LIST:\n\n");
						int size = peer.getFriends().size();
						if (size > 0) {
						for (int i = 0; i < size; i++) {
							terminal.printf(peer.getFriends().get(i) + "\n");
						}
						} else
							terminal.printf("\nYou don't have any friends yet\n");
					} else {
						terminal.printf("First of all you must create your profile\n");
					}
					break;
				case 3:
					newFriendChoice = false;
					if (isProfiled) {
						clearScreen();
						terminal.printf("CHAT IN THE SEMANTIC HARMONY ROOM\n\n");
						terminal.printf("Send *end to close chat window\n\n");
						String message = "";
						while (stringCompare(message, "*end") != 0) {
							message = textIO.newStringInputReader().withDefaultValue(" ").read(" MESSAGE ");
							if (peer.broadcastMessage(profileKey, "\n[" + nickname + "]: " + message))
								terminal.printf("\n");
						}
						clearScreen();
					} else {
						terminal.printf("First of all you must create your profile\n");
					}
					break;
				case 4:
					newFriendChoice = false;
					if (isProfiled) {
						int size = peer.getFriends().size();
						if (size > 0) {
							String message = textIO.newStringInputReader().withDefaultValue(" ").read(" MESSAGE ");
							terminal.printf("\n\nSELECT A FRIEND\n");

							for (int i = 0; i < size; i++) {
								terminal.printf(peer.getFriends().get(i) + "\n");
							}
							int choice = textIO.newIntInputReader().withMaxVal(size).withMinVal(1).read("Your choice");
							int recipientID = choice - 1;
							switch (choice) {
							default:
								if(peer.directMessage(profileKey, SemanticHarmonySNImpl.friendRecipientsList.get(recipientID),
										"\n[DM:" + nickname + "]: " + message))
									terminal.printf("\nThe message has been sent\n");
								else
									terminal.printf("\nSomething went wrong\n");
							}
						} else
							terminal.printf("\nYou don't have any friends yet\n");
					} else {
						terminal.printf("First of all you must create your profile\n");
					}
					break;
				case 5:
					newFriendChoice = false;
					if (isProfiled) {
						terminal.printf("\nARE YOU SURE TO RESET YOUR PROFILE?\n");
						boolean reset = textIO.newBooleanInputReader().withDefaultValue(false).read("reset?");
						if (reset) {
							if (peer.unsubscribeProfileTopic(nickname, profileKey)) {
								terminal.printf("\nNow you can take the test again\n");
								isProfiled = false;
							} else
								terminal.printf("\nSomething went wrong. Please try later\n");
						}
					} else {
						terminal.printf("First of all you must create your profile\n");
					}
					break;
				case 6:
					newFriendChoice = false;
					terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
					boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
					if (exit) {
						peer.leaveNetwork();
						System.exit(0);
					}
					break;
				case 7:
					if (newFriendChoice) {
						SemanticHarmonySNImpl.friendsList.add(SemanticHarmonySNImpl.newUser);
						SemanticHarmonySNImpl.friendRecipientsList.add(SemanticHarmonySNImpl.sender);
						if (!SemanticHarmonySNImpl.newUser.contains("[RE:")) {
							peer.directMessage(profileKey, SemanticHarmonySNImpl.sender,
									"\n[RE:*FRIEND*]: " + nickname);
						}
						newFriendChoice = false;
					} else
						terminal.printf("Invalid value.\n\n");
					break;
				case 8:
					if (!newFriendChoice)
						terminal.printf("Invalid value.\n\n");
					newFriendChoice = false;
					break;
					
				default:
					break;
				}
			}

		} catch (CmdLineException clEx) {
			System.err.println("ERROR: Unable to parse command-line options: " + clEx);
		}

	}

	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE YOUR PROFILE\n");
		terminal.printf("\n2 - GET YOUR FRIENDS LIST\n");
		terminal.printf("\n3 - CHAT IN THE SEMANTIC HARMONY ROOM\n");
		terminal.printf("\n4 - SEND MESSAGE TO A FRIEND\n");
		terminal.printf("\n5 - RESET PROFILE\n");
		terminal.printf("\n6 - EXIT\n");

	}

}
