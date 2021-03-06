/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_util_abstracttest_h
#define __inc_og_language_util_abstracttest_h

// Generic testing abstraction

#include "Logging.h"

#ifdef __cplusplus_cli
using namespace Microsoft::VisualStudio::TestTools::UnitTesting;
#endif /* ifdef __cplusplus_cli */

class CAbstractTest {
private:
	const TCHAR *m_pszName;
	bool m_bAutomatic;
public:
#ifndef __cplusplus_cli
	CAbstractTest (bool bAutomatic, const TCHAR *pszName);
	~CAbstractTest ();
	virtual void Run () = 0;
	virtual void BeforeAll () { }
	virtual void Before () { }
	virtual void After ();
	virtual void AfterAll () { }
	static void Main (int argc, TCHAR **argv);
#endif /* ifndef __cplusplus_cli */
	static void InitialiseLogs ();
	static void Fail () {
#ifdef __cplusplus_cli
    	Assert::Fail ();
#else
    	exit (1);
#endif /* ifdef __cplusplus_cli */
	}
};

#define ASSERT(_expr_) \
	if (!(_expr_)) { \
		LOGFATAL (TEXT ("Assertion ") << __LINE__ << TEXT (" failed")); \
		CAbstractTest::Fail (); \
	}

#ifdef __cplusplus_cli
#define BEGIN_TESTS_(automatic, label) \
	static bool s_bAutomatic = automatic; \
	[TestClass] \
	public ref class C##label { \
	public:
#define BEGIN_TESTS(label) BEGIN_TESTS_(true, label)
#define MANUAL_TESTS(label) BEGIN_TESTS_(false, label)
#define TEST(proc) \
		[TestMethod] \
		void Test##proc () { \
			CAbstractTest::InitialiseLogs (); \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping test ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Running test ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Test ") << TEXT (#proc) << TEXT (" complete")); \
		}
#define BEFORE_TEST(proc) \
		[TestInitialize] \
		void Before##proc () { \
			CAbstractTest::InitialiseLogs (); \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping pre-test ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting pre-test ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Pre-test ") << TEXT (#proc) << TEXT (" complete")); \
		}
#define AFTER_TEST(proc) \
		[TestCleanup] \
		void After##proc () { \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping post-test ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting post-test ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Post-test ") << TEXT (#proc) << TEXT (" complete")); \
		}
#define BEFORE_ALL_TESTS(proc) \
		[ClassInitialize] \
		static void BeforeAll##proc () { \
			CAbstractTest::InitialiseLogs (); \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping before-all ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting before-all ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("Before-all ") << TEXT (#proc) << TEXT (" complete")); \
		}
#define AFTER_ALL_TESTS(proc) \
		[ClassCleanup] \
		static void AfterAll##proc () { \
			if (!s_bAutomatic) { \
				LOGINFO (TEXT ("Skipping after-all ") << TEXT (#proc)); \
				return; \
			} \
			LOGINFO (TEXT ("Starting after-all ") << TEXT (#proc)); \
			::proc (); \
			LOGINFO (TEXT ("After-all ") << TEXT (#proc) << TEXT (" complete")); \
		}
#define END_TESTS \
	};
#else
#define BEGIN_TESTS_(automatic, label) \
	static class C##label : public CAbstractTest { \
	public: \
		C##label () : CAbstractTest (automatic, TEXT (#label)) { } \
		void Run () { \
			LOGINFO (TEXT ("Beginning ") << TEXT (#label));
#define BEGIN_TESTS(label) BEGIN_TESTS_(true, label)
#define MANUAL_TESTS(label) BEGIN_TESTS_(false, label)
#define TEST(proc) \
			LOGINFO (TEXT ("Running test ") << TEXT (#proc)); \
			Before (); \
			::proc (); \
			After (); \
			LOGINFO (TEXT ("Test ") << TEXT (#proc) << TEXT (" complete"));
#define BEFORE_TEST(proc) \
		} \
		void Before () { \
			LOGDEBUG (TEXT ("Starting pre-test ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("Pre-test ") << TEXT (#proc) << TEXT (" complete"));
#define AFTER_TEST(proc) \
		} \
		void After () { \
			LOGDEBUG (TEXT ("Starting post-test ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("Post-test ") << TEXT (#proc) << TEXT (" complete")); \
			CAbstractTest::After ();
#define BEFORE_ALL_TESTS(proc) \
		} \
		void BeforeAll () { \
			LOGDEBUG (TEXT ("Starting before-all ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("Before-all ") << TEXT (#proc) << TEXT (" complete"));
#define AFTER_ALL_TESTS(proc) \
		} \
		void AfterAll () { \
			LOGDEBUG (TEXT ("Starting after-all ") << TEXT (#proc)); \
			::proc (); \
			LOGDEBUG (TEXT ("After-all ") << TEXT (#proc) << TEXT (" complete"));
#define END_TESTS \
		} \
	} g_o##__LINE__;
#endif /* ifdef __cplusplus_cli */

#endif /* ifndef __inc_og_language_util_abstracttest_h */
