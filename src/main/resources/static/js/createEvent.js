let dateNow;

$(document).ready(function () {
    $("#createContract").submit(function (e) {
        e.preventDefault();
        const form = this;
        createEvent().then(function () {
            form.submit();
        })
    });
});

async function createEvent() {
    dateNow = Math.floor(Date.now() / 1000);
    const calendarTimeBasedEventJson =
        await signEvent(
            await generateCTBEventJson());

    document.getElementById('calendarTimeBasedEventJson').value = JSON.stringify(calendarTimeBasedEventJson);

    const classifiedListingEventJson =
        await signEvent(
            await generateCLEventJson(calendarTimeBasedEventJson.id));

    document.getElementById('classifiedListingEventJson').value = JSON.stringify(classifiedListingEventJson);
}

async function generateCTBEventJson() {
    const content = $("#content");
    return {
        id: '',
        kind: 31923,
        created_at: dateNow,
        content: ("CTBEvent content field: " + content.val()),
        tags: [
            ['d', $("#ctbEventUuid").val()],
            ['title', "CTBEvent title field: " + content.val()],
            ['start', dateNow+100000],
            ['summary', "CTBEvent summary field: " + content.val()],
            ['location', "CTBEvent location field"],
            ['p', await window.nostr.getPublicKey(), "wss://localhost:5555", $("#role").val()]
        ],
        pubkey: '',
        sig: ''
    }
}

async function generateCLEventJson(ctbEventId) {
    const content = $("#content");
    return {
        id: '',
        kind: 30402,
        created_at: dateNow,
        content: "CLEvent content field: " + content.val(),
        tags: [
            ['subject', "CLEvent subject field: " + content.val()],
            ['title', "CLEvent title field: " + content.val()],
            ['published_at', dateNow],
            ['summary', "CLEvent summary field: " + content.val()],
            ['location', "CLEvent location field"],
            ['price', $("#payoutAmount").val(), "BTC", "1"],
            ['p', await window.nostr.getPublicKey(), "wss://localhost:5555", $("#role").val()],
            ['a', "31923:" + await window.nostr.getPublicKey() + ":" + ctbEventId]
        ],
        pubkey: '',
        sig: ''
    }
}

async function signEvent(event) {
    console.log('signEvent() input: \n\n' + event + '\n\n');
    const signedPopulatedEvent = await window.nostr.signEvent(event);
    console.log('signEvent() output: ' + signedPopulatedEvent);
    return signedPopulatedEvent;
}
