import { readFileSync } from "node:fs";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  Timestamp,
  doc,
  getDoc,
  runTransaction,
  setDoc,
  updateDoc,
} from "firebase/firestore";

const projectId = "demo-anlikdeprem";
const testEnvironment = await initializeTestEnvironment({
  projectId,
  firestore: { rules: readFileSync("firestore.rules", "utf8") },
});

const alice = testEnvironment.authenticatedContext("alice").firestore();
const bob = testEnvironment.authenticatedContext("bob").firestore();
const guest = testEnvironment.unauthenticatedContext().firestore();
const now = Date.now();

const validDevice = (uid) => ({
  fcmToken: "test-fcm-token-that-is-long-enough",
  deviceId: uid,
  deviceModel: "CI emulator",
  osVersion: "test",
  lastActive: Timestamp.fromMillis(now),
  expiresAt: Timestamp.fromMillis(now + 90 * 24 * 60 * 60 * 1000),
});

async function submitFeltReport(database, earthquakeId, uid) {
  const reportRef = doc(database, "felt_reports", earthquakeId);
  const reporterRef = doc(database, "felt_reports", earthquakeId, "reporters", uid);
  return runTransaction(database, async (transaction) => {
    const reporter = await transaction.get(reporterRef);
    if (reporter.exists()) return false;
    const report = await transaction.get(reportRef);
    transaction.set(reportRef, { count: (report.data()?.count ?? 0) + 1 });
    transaction.set(reporterRef, { createdAt: Timestamp.fromMillis(Date.now()) });
    return true;
  });
}

try {
  await assertFails(setDoc(doc(guest, "devices", "guest"), validDevice("guest")));
  await assertSucceeds(setDoc(doc(alice, "devices", "alice"), validDevice("alice")));
  await assertFails(setDoc(doc(alice, "devices", "bob"), validDevice("bob")));
  await assertFails(setDoc(doc(alice, "devices", "alice"), {
    ...validDevice("alice"),
    fcmToken: "x".repeat(5000),
  }));
  await assertFails(setDoc(doc(alice, "devices", "alice"), {
    ...validDevice("alice"),
    unexpected: true,
  }));

  await assertSucceeds(submitFeltReport(alice, "event-1", "alice"));
  const duplicateResult = await assertSucceeds(submitFeltReport(alice, "event-1", "alice"));
  if (duplicateResult !== false) throw new Error("Duplicate felt report must be ignored");
  await assertFails(updateDoc(doc(alice, "felt_reports", "event-1"), { count: 2 }));
  await assertSucceeds(submitFeltReport(bob, "event-1", "bob"));
  const aggregate = await assertSucceeds(getDoc(doc(alice, "felt_reports", "event-1")));
  if (aggregate.data()?.count !== 2) throw new Error("Felt report count must be 2");
  await assertFails(getDoc(doc(guest, "felt_reports", "event-1", "reporters", "alice")));
  await assertFails(getDoc(doc(bob, "felt_reports", "event-1", "reporters", "alice")));
  await assertSucceeds(getDoc(doc(alice, "felt_reports", "event-1", "reporters", "alice")));
  await assertFails(setDoc(doc(alice, "felt_reports", "event-1", "reporters", "alice"), {
    createdAt: Timestamp.fromMillis(Date.now()),
  }));

  const validComment = doc(alice, "felt_reports", "event-1", "comments", "valid");
  await assertSucceeds(setDoc(validComment, {
    text: "Test comment",
    timestampMillis: Date.now(),
    reported: false,
  }));
  await assertFails(setDoc(doc(alice, "felt_reports", "event-1", "comments", "too-long"), {
    text: "x".repeat(301),
    timestampMillis: Date.now(),
    reported: false,
  }));
  await assertFails(setDoc(doc(alice, "felt_reports", "event-1", "comments", "future"), {
    text: "Future comment",
    timestampMillis: Date.now() + 60 * 60 * 1000,
    reported: false,
  }));
  await assertFails(setDoc(doc(guest, "felt_reports", "event-1", "comments", "guest"), {
    text: "Unauthenticated comment",
    timestampMillis: Date.now(),
    reported: false,
  }));
  await assertFails(setDoc(doc(alice, "felt_reports", "event-1", "comments", "extra-field"), {
    text: "Unexpected field",
    timestampMillis: Date.now(),
    reported: false,
    uid: "alice",
  }));
  await assertSucceeds(updateDoc(validComment, { reported: true }));
  await assertFails(updateDoc(validComment, { reported: true }));
  await assertFails(updateDoc(doc(alice, "felt_reports", "event-1", "comments", "valid"), {
    text: "Changed after creation",
    reported: true,
  }));

  await assertSucceeds(setDoc(doc(alice, "felt_reports", "event-1", "viewers", "alice"), {
    lastSeenMillis: Date.now(),
    expiresAt: Timestamp.fromMillis(Date.now() + 10 * 60 * 1000),
  }));
  await assertFails(setDoc(doc(alice, "felt_reports", "event-1", "viewers", "alice"), {
    lastSeenMillis: Date.now(),
    expiresAt: Timestamp.fromMillis(Date.now() + 24 * 60 * 60 * 1000),
  }));
  await assertFails(setDoc(doc(alice, "felt_reports", "event-1", "viewers", "bob"), {
    lastSeenMillis: Date.now(),
    expiresAt: Timestamp.fromMillis(Date.now() + 10 * 60 * 1000),
  }));
  await assertFails(setDoc(doc(guest, "felt_reports", "event-1", "viewers", "guest"), {
    lastSeenMillis: Date.now(),
    expiresAt: Timestamp.fromMillis(Date.now() + 10 * 60 * 1000),
  }));

  console.log("Firestore security rules tests passed");
} finally {
  await testEnvironment.cleanup();
}
