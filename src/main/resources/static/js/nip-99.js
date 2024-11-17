$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $("#submit99").prop("disabled", false);
    $("#submit99").click(async () => createEvent(await generateCTBEventJson()));
});

async function createEvent(typeScriptEvent) {
    // const form = document.querySelector("#contractSubmit");

    // const tempval = document.getElementById('ctbEventDto');
    // console.log("*****************");
    // console.log("*****************");
    // console.log(tempval);
    // console.log("----");
    // console.log("*****************");
    // console.log("*****************");

    // form.setAttribute(
    //     "ctbEventDto",
    //     $.extend(
    //         document.getElementById('ctbEventDto'),
    //         // await window.nostr.signEvent(
    //         //     generateCTBEventJson(dateNow)
    //     )
    // );

    signEvent(typeScriptEvent)
        .then(ctbEventJson => $
            .extend(
                document.getElementById('ctbEventDto'),
                ctbEventJson));

    // .then(extendedCTBEventDto => form
    //     .setAttribute(
    //         "ctbEventDto", extendedCTBEventDto)))

    // .then(ctbEventJson => sendData(fullyPopulatedSignedEvent));

    // .then(window.nostr
    //     .signEvent(
    //         generateCLEventJson(
    //             dateNow,
    //             ctbEventJson['id']))
    //     .then(clEventJson => $
    //         .extend(
    //             document.getElementById('clEventDto'),
    //             clEventJson))))

    // .then(sendData())

    // form.setAttribute(
    //     "clEventDto",
    //     // $.extend(
    //     //     document.getElementById('clEventDto'),
    //     await window.nostr.signEvent(
    //         generateCLEventJson(
    //             dateNow,
    //             // JSON.parse(
    //             await window.nostr.signEvent(
    //                 generateCTBEventJson(dateNow))
    //                 // )
    //                 ['id'])
    //     )
    //     // )
    // );

}

async function signEvent(event) {
    console.log('signEvent() input: \n\n' + event + '\n\n');
    var signedPopulatedEvent = await window.nostr.signEvent(event);
    console.log('signEvent() output: ' + signedPopulatedEvent);
    return signedPopulatedEvent;
}

async function generateCTBEventJson() {
    const dateNow = Math.floor(Date.now() / 1000);
    // const ctbEventTags = [
    //     ['subject', "CTBEvent subject field: " + $("#content").val()],
    //     ['title', "CTBEvent title field: " + $("#content").val()],
    //     ['published_at', dateNow],
    //     ['summary', "CTBEvent summary field: " + $("#content").val()],
    //     ['location', "CTBEvent location field"],
    //     ['p', await window.nostr.getPublicKey(), "wss://localhost:5555", $("#role").val()]
    //     // ['p', "9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224", "wss://localhost:5555", $("#role").val()]
    // ];

    console.log("generateCTBEventJson() checkpoint (since prohibited from console logging CTBEvent event JSON)");

    return {
        id: '',
        kind: 31923,
        created_at: dateNow,
        content: ("CTBEvent content field: " + $("#content").val()),
        tags: [
            ['subject', "CTBEvent subject field: " + $("#content").val()],
            ['title', "CTBEvent title field: " + $("#content").val()],
            ['published_at', dateNow],
            ['summary', "CTBEvent summary field: " + $("#content").val()],
            ['location', "CTBEvent location field"],
            ['p', await window.nostr.getPublicKey(), "wss://localhost:5555", $("#role").val()]
            // ['p', "9cf26cf9e1635723fd4dca4db6c25aac99bda57d1961d02c83d47cc26ea0b224", "wss://localhost:5555", $("#role").val()]
        ],
        pubkey: '',
        sig: ''
    }
}

async function generateCLEventJson(dateNow, ctbEventId) {
    const cleEventTags = [
        ['subject', "CLEvent subject field: " + $("#content").val()],
        ['title', "CLEvent title field: " + $("#content").val()],
        ['published_at', dateNow],
        ['summary', "CLEvent summary field: " + $("#content").val()],
        ['location', "CLEvent location field"],
        ['price', $("payoutAmount").val(), "BTC", "1"],
        ['p', await window.nostr.getPublicKey(), "ws://localhost:5555", $("#role").val()],
        // ['p', "111df01ca1aa9d6f1c35953833bbe6d99a0c85b73af222e6bd305b51f2749f6f", "ws://localhost:5555", $("#role").val()]
        ['a', "31923: " + await window.nostr.getPublicKey() + ":" + ctbEventId]
    ];

    const returnval = {
        id: '',
        kind: 30402,
        created_at: dateNow,
        content: "CLEvent content field: " + $("#content").val(),
        tags: cleEventTags,
        pubkey: '',
        sig: ''
    }

    console.log("generateCLEventJson() checkpoint (since prohibited from console logging CLEventJson event JSON)");
    return returnval;
}

async function sendData(data) {
    const response = await fetch("contract/create", {
        method: "POST",
        // Set the FormData instance as the request body
        body: data,
    });
    console.log(await response.json());
}
