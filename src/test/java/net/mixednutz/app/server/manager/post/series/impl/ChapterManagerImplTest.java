package net.mixednutz.app.server.manager.post.series.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.mixednutz.app.server.entity.post.series.Chapter;

public class ChapterManagerImplTest {

	ChapterManagerImpl manager;
	
	@BeforeEach
	public void setup() {
		manager = new ChapterManagerImpl();
	}
	
	@Test
	public void wordCount() {
		Chapter chapter = new Chapter();
		chapter.setBody("One Two Three");
		assertEquals(3, manager.wordCount(chapter));
		
		chapter = new Chapter();
		chapter.setBody("<b>One</b> Two Three Four");
		assertEquals(4, manager.wordCount(chapter));
		
		chapter = new Chapter();
		chapter.setBody("<a href=\"#\">One</b> Two Three Four\nFive");
		assertEquals(5, manager.wordCount(chapter));
		
		chapter = new Chapter();
		chapter.setBody(" Did you ever look back at your youth and fantasize about redoing it a second time with all of your future knowledge and experiences? What would you do if given a second chance? Would you take the path you didn’t take the first time?  That’s where my story starts Or ends depending on your point of view Time travel gets confusing like that  Friday April 6 2018  My name is Anthony Demarco Friends and family call me Tony You can call me Tony too since you’re reading this I’m 37 years old and as of this afternoon I’m a divorced man My wife and I split as amicably as possible After having been married for almost 3 years we both mutually came to the conclusion that we were no longer the right fit for each other She claimed I never really loved her and I only settled down with her because she was the longest relationship I had prior to proposing I denied that accusation until the end But a part of me wondered if she was right Months after the wedding the excitement started to fade and we realized we were different people heading in different directions After the constant fights there was a point where I stopped trying and the romance faded I mean we’ve been separated for a while and I knew this was coming but it still hurts I’ve always been more interested in my career I’m a web designer for a local consulting company building applications for various non-tech companies I actually love what I do It’s been my hobby since I was a teenager My only regret is that I work for someone else instead of myself As such I’m overworked and underpaid and I’ve certainly been passed over for more than one promotion because I’m not out kissing up to upper management There is a “game” to be played in “Corporate America” and I refuse to play it When my marriage started crumbling I buried my head into my work which only made it crumble even faster Today I signed the divorce papers I walked into my quiet house threw down my keys and unopened mail and fell down on the sole sofa in the living room The house is quiet except for the hum of the heat Pulling out my cell phone from my back pocket I start to check my missed voicemails from the day  “Hi Tony it’s mom I’m just calling to see how you’re doing With all that is going on maybe you should take some time off to come visit Me and Dad haven’t seen you in a long time”  - Next Message -  “Hey man It’s Ron Just checking up on you Why don’t we get together for drinks Just us guys Give me a call back ”  - End of Messages - I didn’t feel like calling either back right now I was not looking forward to the inevitable guilt trip my mom is going to put on me I already know the failed marriage is my fault I didn't need her rubbing it in I'm a grown-ass man I don't need to run back to my parents when my social life crashes and burns As for Ron my best friend I didn’t feel like driving the 40 minutes to his house right now I wanted to wallow in my own self-pity I reached over to check the mail Bill Junk Bill Circular I got up to the kitchen and deposited them in the trash I turned to the fridge to fish out a beer My fridge was practically empty My wife hasn't lived here here in 3 months so I wasn’t expecting a full fridge I hadn’t been to the store in at least a week I thought there was some leftover pizza in there I guess I ate that last night I was out of leftovers; out of beer “Wonderful” I mumbled to myself closing the fridge door When I closed the fridge door a piece of card-stock fell down from the top of the fridge It must’ve been moved off the front of the fridge during my wife’s move and shoved up top It was a postcard invitation to my high school reunion It was adorned with a red and black picture of my High School Mascot – the Bull “It’s Reunion Time” the fancy text read “The Class of 1998 officially invites you to our 20th High School Reunion” The date read “Saturday April 7th 2018” That’s tomorrow I recalled actually RSVPing to this months ago Before my separation with my wife I was actually looking forward to going to this with my wife in tow “Well there goes those plans” I mumbled to myself Aside from my younger sister Cindy the only person from High School I really kept in touch with was Ron Everyone else I could care less about I wasn’t very popular in high school I was very much an introvert with one or two friends Granted I was in the marching band and Ron had peer pressured me into playing JV Tennis but that was it I never went to parties I never had a girlfriend Never dated I barely went to the prom - and I remembered it was awkward and I didn’t enjoy it Despite not caring what those people from 20 years ago thought about me there was a part of me that wanted to show up with a great job and a loving wife to show all of those losers that I turned out pretty cool Well I certainly wasn’t going to go stag now \"OK\" I told myself \"Dinner Time I could either order pizza for the 2nd time this week and Netflix and chill with myself or I could go out to the pub and try to redeem my Friday night\" I chose the latter Only because I was out of beer too * * * I walked into the local pub It was a small hole in the wall I started coming to with my wife when we first moved here They served pretty good burgers and some nights had live entertainment Tonight I was just looking for dinner and a drink I took an open seat at the bar far away from everyone else I looked around and there were definitely people at tables eating and enjoying each other's company Near the front right where I walked in there were a few high school looking guys testing musical equipment Great I must’ve come during teen-night or something A couple of bangs on a drum and some strums of the guitars to test the volume Maybe this was a bad idea I flagged the bartender A 20 something woman with green hair and green lipstick walked over \"What can I do for you?\" She asked \"I'll have that IPA on tap And a shot of whiskey\" I responded \"Plus I was going to get a bacon cheese burger lettuce tomatoes Cooked Medium\" \"Sure thing\" she replied As she walked away to put my order in a group of teenagers walked into the restaurant A mix of guys and girls gathered around the 2 guys setting up High-fives and fist bumps Clearly they knew each other The TVs were tuned to ESPN and CNN I wasn't interested in any spring sports and CNN was talking politics My beer and shot arrived at that moment I picked up the shot glass tapped the bar-top with the glass and downed it The band started to play a cover of a Chainsmokers song and the teens in the front cheered I ate the burger and while glancing up at the TV on the wall seeing if there’s anything new up there ESPN was talking about the up and coming NFL draft in a few weeks and the various college prospects Having been raised in New Jersey I was a New York Giants fan so I had my favorites on who they should pick The experts had differing opinions I washed the burger down with my beer I glanced back to the teens They were nothing but smiles There was one couple holding hands as if this was their date night One guy was playing on his phone Another guy was using his phone to video record the performance One girl was just staring at the lead singer lost in her own world The lead singer was definitely staring back at her as if he was singing directly to her I made eye contact with the bartender who was delivering a tray of sodas to the teens I made a scribble motion on my hand to indicate I wanted the check She came by shortly with the bill and I handed her my credit card “I didn't expect it to be teen night” I said to the bartender \"Yeah\" she said \"Typically we'd have them in here during the week but the schedule got a little messed up They seem to be behaving themselves This seems to be one of the better bands anyway\" \"Well at least they're having more fun than I am\" I stated \"Whoa\" she said \"Was my service that bad?\" Realizing I had just insulted her I back tracked \"Oh no That's not what I mean at all I'm just having a bad day Was hoping for just a little quiet time\" \"I see\" she said about to turn away I felt stupid as if I had to explain myself and I continued \"I mean look at them Not a care in the world The girl and the singer are clearly eye fucking in public\" She turned back towards me with her eyebrow raised Clearly I just spewed word vomit Maybe this is why I have a hard time talking to people “Envious?” she stated “Pardon?” I replied \"You’re stuck in a rut\" she continued \"and you’re envious of that group over there having fun Didn't you do the same thing when you were their age?” “Well I was in a rut then too Just seeing them brings back some memories of when I was them It was a frustrating time for me” \"Well teenagers are horny and in a perpetual state of frustration What made you any different?\" \"I was an introvert I wanted to socialize I just couldn't I was paralyzed from saying the right thing and putting myself out there I rarely took risks I settled on what was comfortable I was comfortable staying on the sidelines\" She didn't say anything and let me continue \"Maybe if I did things differently I wouldn’t be in such a rut today Man if I could do it all over again - with the last 20 years of experience - things would be different\" \"Oh really?\" She was intrigued \"What would you do differently?\" \"Oh I'd be more outgoing\" \"You actually seem outgoing to me right now\" She said \"You're clearly lacking confidence from whatever bad day you had Are you sure you're as introverted as you're saying?\" \"Well I slowly started coming out of my shell during and after college I found a group of friends that pushed me out of my shell I got a job and money I was pressured into joining my company softball team That’s where I met my wife – I mean ex-wife I really didn't date anyone until I met her a few years ago I was just too introverted and too concerned with how people viewed me to take risks\" \"So Mr Outgoing\" she said moving closer to me \"If you had to do it all over again what would you do differently?\"");
		assertEquals(1950, manager.wordCount(chapter));
	}
	
	@Test
	public void readingTime() {
		Chapter chapter = new Chapter();
		chapter.setBody(" Did you ever look back at your youth and fantasize about redoing it a second time with all of your future knowledge and experiences? What would you do if given a second chance? Would you take the path you didn’t take the first time?  That’s where my story starts Or ends depending on your point of view Time travel gets confusing like that  Friday April 6 2018  My name is Anthony Demarco Friends and family call me Tony You can call me Tony too since you’re reading this I’m 37 years old and as of this afternoon I’m a divorced man My wife and I split as amicably as possible After having been married for almost 3 years we both mutually came to the conclusion that we were no longer the right fit for each other She claimed I never really loved her and I only settled down with her because she was the longest relationship I had prior to proposing I denied that accusation until the end But a part of me wondered if she was right Months after the wedding the excitement started to fade and we realized we were different people heading in different directions After the constant fights there was a point where I stopped trying and the romance faded I mean we’ve been separated for a while and I knew this was coming but it still hurts I’ve always been more interested in my career I’m a web designer for a local consulting company building applications for various non-tech companies I actually love what I do It’s been my hobby since I was a teenager My only regret is that I work for someone else instead of myself As such I’m overworked and underpaid and I’ve certainly been passed over for more than one promotion because I’m not out kissing up to upper management There is a “game” to be played in “Corporate America” and I refuse to play it When my marriage started crumbling I buried my head into my work which only made it crumble even faster Today I signed the divorce papers I walked into my quiet house threw down my keys and unopened mail and fell down on the sole sofa in the living room The house is quiet except for the hum of the heat Pulling out my cell phone from my back pocket I start to check my missed voicemails from the day  “Hi Tony it’s mom I’m just calling to see how you’re doing With all that is going on maybe you should take some time off to come visit Me and Dad haven’t seen you in a long time”  - Next Message -  “Hey man It’s Ron Just checking up on you Why don’t we get together for drinks Just us guys Give me a call back ”  - End of Messages - I didn’t feel like calling either back right now I was not looking forward to the inevitable guilt trip my mom is going to put on me I already know the failed marriage is my fault I didn't need her rubbing it in I'm a grown-ass man I don't need to run back to my parents when my social life crashes and burns As for Ron my best friend I didn’t feel like driving the 40 minutes to his house right now I wanted to wallow in my own self-pity I reached over to check the mail Bill Junk Bill Circular I got up to the kitchen and deposited them in the trash I turned to the fridge to fish out a beer My fridge was practically empty My wife hasn't lived here here in 3 months so I wasn’t expecting a full fridge I hadn’t been to the store in at least a week I thought there was some leftover pizza in there I guess I ate that last night I was out of leftovers; out of beer “Wonderful” I mumbled to myself closing the fridge door When I closed the fridge door a piece of card-stock fell down from the top of the fridge It must’ve been moved off the front of the fridge during my wife’s move and shoved up top It was a postcard invitation to my high school reunion It was adorned with a red and black picture of my High School Mascot – the Bull “It’s Reunion Time” the fancy text read “The Class of 1998 officially invites you to our 20th High School Reunion” The date read “Saturday April 7th 2018” That’s tomorrow I recalled actually RSVPing to this months ago Before my separation with my wife I was actually looking forward to going to this with my wife in tow “Well there goes those plans” I mumbled to myself Aside from my younger sister Cindy the only person from High School I really kept in touch with was Ron Everyone else I could care less about I wasn’t very popular in high school I was very much an introvert with one or two friends Granted I was in the marching band and Ron had peer pressured me into playing JV Tennis but that was it I never went to parties I never had a girlfriend Never dated I barely went to the prom - and I remembered it was awkward and I didn’t enjoy it Despite not caring what those people from 20 years ago thought about me there was a part of me that wanted to show up with a great job and a loving wife to show all of those losers that I turned out pretty cool Well I certainly wasn’t going to go stag now \"OK\" I told myself \"Dinner Time I could either order pizza for the 2nd time this week and Netflix and chill with myself or I could go out to the pub and try to redeem my Friday night\" I chose the latter Only because I was out of beer too * * * I walked into the local pub It was a small hole in the wall I started coming to with my wife when we first moved here They served pretty good burgers and some nights had live entertainment Tonight I was just looking for dinner and a drink I took an open seat at the bar far away from everyone else I looked around and there were definitely people at tables eating and enjoying each other's company Near the front right where I walked in there were a few high school looking guys testing musical equipment Great I must’ve come during teen-night or something A couple of bangs on a drum and some strums of the guitars to test the volume Maybe this was a bad idea I flagged the bartender A 20 something woman with green hair and green lipstick walked over \"What can I do for you?\" She asked \"I'll have that IPA on tap And a shot of whiskey\" I responded \"Plus I was going to get a bacon cheese burger lettuce tomatoes Cooked Medium\" \"Sure thing\" she replied As she walked away to put my order in a group of teenagers walked into the restaurant A mix of guys and girls gathered around the 2 guys setting up High-fives and fist bumps Clearly they knew each other The TVs were tuned to ESPN and CNN I wasn't interested in any spring sports and CNN was talking politics My beer and shot arrived at that moment I picked up the shot glass tapped the bar-top with the glass and downed it The band started to play a cover of a Chainsmokers song and the teens in the front cheered I ate the burger and while glancing up at the TV on the wall seeing if there’s anything new up there ESPN was talking about the up and coming NFL draft in a few weeks and the various college prospects Having been raised in New Jersey I was a New York Giants fan so I had my favorites on who they should pick The experts had differing opinions I washed the burger down with my beer I glanced back to the teens They were nothing but smiles There was one couple holding hands as if this was their date night One guy was playing on his phone Another guy was using his phone to video record the performance One girl was just staring at the lead singer lost in her own world The lead singer was definitely staring back at her as if he was singing directly to her I made eye contact with the bartender who was delivering a tray of sodas to the teens I made a scribble motion on my hand to indicate I wanted the check She came by shortly with the bill and I handed her my credit card “I didn't expect it to be teen night” I said to the bartender \"Yeah\" she said \"Typically we'd have them in here during the week but the schedule got a little messed up They seem to be behaving themselves This seems to be one of the better bands anyway\" \"Well at least they're having more fun than I am\" I stated \"Whoa\" she said \"Was my service that bad?\" Realizing I had just insulted her I back tracked \"Oh no That's not what I mean at all I'm just having a bad day Was hoping for just a little quiet time\" \"I see\" she said about to turn away I felt stupid as if I had to explain myself and I continued \"I mean look at them Not a care in the world The girl and the singer are clearly eye fucking in public\" She turned back towards me with her eyebrow raised Clearly I just spewed word vomit Maybe this is why I have a hard time talking to people “Envious?” she stated “Pardon?” I replied \"You’re stuck in a rut\" she continued \"and you’re envious of that group over there having fun Didn't you do the same thing when you were their age?” “Well I was in a rut then too Just seeing them brings back some memories of when I was them It was a frustrating time for me” \"Well teenagers are horny and in a perpetual state of frustration What made you any different?\" \"I was an introvert I wanted to socialize I just couldn't I was paralyzed from saying the right thing and putting myself out there I rarely took risks I settled on what was comfortable I was comfortable staying on the sidelines\" She didn't say anything and let me continue \"Maybe if I did things differently I wouldn’t be in such a rut today Man if I could do it all over again - with the last 20 years of experience - things would be different\" \"Oh really?\" She was intrigued \"What would you do differently?\" \"Oh I'd be more outgoing\" \"You actually seem outgoing to me right now\" She said \"You're clearly lacking confidence from whatever bad day you had Are you sure you're as introverted as you're saying?\" \"Well I slowly started coming out of my shell during and after college I found a group of friends that pushed me out of my shell I got a job and money I was pressured into joining my company softball team That’s where I met my wife – I mean ex-wife I really didn't date anyone until I met her a few years ago I was just too introverted and too concerned with how people viewed me to take risks\" \"So Mr Outgoing\" she said moving closer to me \"If you had to do it all over again what would you do differently?\"");
		assertEquals(9, manager.readingTime(chapter));
	}
	
}
