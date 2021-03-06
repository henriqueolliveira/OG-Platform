/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_connector_client_h
#define __inc_og_language_connector_client_h

// Manages and communicates with the Java client

#include "JVM.h"
#include "Pipes.h"
#include <Util/Mutex.h>
#include <Util/Semaphore.h>
#include <Util/Thread.h>

enum ClientServiceState {
	STARTING,
	RUNNING,
	STOPPING,
	POISONED,
	ERRORED,
	STOPPED
};

// The "const" modifier applies to the start/stop state. A "const" client can send and receive messages.
class CClientService {
public:
	// Callback for state changes (e.g. to be notified when fully connected). The callback is part of the
	// main service event thread so should not block or call back to other blocking operations (like
	// sending a message).
	class CStateChange {
	protected:
		friend class CClientService;
		virtual void OnStateChange (ClientServiceState ePreviousState, ClientServiceState eNewState) = 0;
	};
	// Callback for messages received. The callback is part of the main service event thread so should not
	// block or call back to other blocking operations (like sending a message).
	class CMessageReceived {
	protected:
		friend class CClientService;
		virtual void OnMessageReceived (FudgeMsg msg) = 0;
	};
private:
	class CRunnerThread;
	// Attributes
	TCHAR *m_pszLanguageID;
	mutable CAtomicInt m_oRefCount;
	mutable CMutex m_oStateMutex;
	CMutex m_oStopMutex;
	ClientServiceState m_eState;
	CMutex m_oStateChangeMutex;
	CStateChange *m_poStateChangeCallback;
	CMutex m_oMessageReceivedMutex;
	CMessageReceived *m_poMessageReceivedCallback;
	CRunnerThread *m_poRunner;
	mutable CSemaphore m_oPipesSemaphore;
	CClientPipes *m_poPipes;
	CClientJVM *m_poJVM;
	unsigned long m_lSendTimeout;
	unsigned long m_lShortTimeout;
	// Private constructor - stops stack allocation
	CClientService (const TCHAR *pszLanguageID);
	~CClientService ();
	// Thread runner callbacks
	void ClosePipes ();
	bool ConnectPipes ();
	bool CreatePipes ();
	bool DispatchAndRelease (FudgeMsgEnvelope env);
	bool IsFirstConnection () const { return m_lShortTimeout != 0; }
	void FirstConnectionOk () { m_lSendTimeout = m_lShortTimeout; m_lShortTimeout = 0; }
	bool HeartbeatNeeded (unsigned long lTimeout) const { return GetTickCount () - m_poPipes->GetLastWrite () >= lTimeout; }
	FudgeMsgEnvelope Recv (unsigned long lTimeout);
	bool Send (int cProcessingDirectives, FudgeMsg msg) const;
	bool SendPoison ();
	bool SetState (ClientServiceState eNewState);
	bool StartJVM ();
	bool StopJVM ();
public:
	// Creation
	static CClientService *Create (const TCHAR *pszLanguageID) { return new CClientService (pszLanguageID); }
	void Retain () const { m_oRefCount.IncrementAndGet (); }
	static void Release (const CClientService *poClientService) { if (!poClientService->m_oRefCount.DecrementAndGet ()) delete poClientService; }
	// Control
	bool Stop ();
	bool Start ();
	ClientServiceState GetState () const;
	bool Send (FudgeMsg msg) const;
	void SetStateChangeCallback (CStateChange *poCallback);
	void SetMessageReceivedCallback (CMessageReceived *poCallback);
};

#endif /* ifndef __inc_og_language_connect_client_h */
