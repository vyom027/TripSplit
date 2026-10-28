const { onCall, HttpsError } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

exports.joinWithToken = onCall(async (request) => {
  const token = request.data && request.data.token;
  if (!token || typeof token !== "string") {
    throw new HttpsError("invalid-argument", "Missing invite token");
  }

  const members = await db.collectionGroup("members").where("inviteToken", "==", token).limit(1).get();
  if (!members.empty) {
    const doc = members.docs[0];
    const data = doc.data() || {};
    return {
      tripId: data.tripId || doc.ref.parent.parent.id,
      memberId: doc.id,
      role: data.role || "MEMBER",
    };
  }

  const trips = await db.collection("trips").where("managerRecoveryToken", "==", token).limit(1).get();
  if (trips.empty) {
    throw new HttpsError("not-found", "Unknown invite link");
  }
  const trip = trips.docs[0];
  const tripData = trip.data() || {};
  return {
    tripId: trip.id,
    memberId: tripData.managerMemberId || "",
    role: "MANAGER",
  };
});
