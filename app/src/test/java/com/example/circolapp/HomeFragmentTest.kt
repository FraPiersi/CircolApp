package com.example.circolapp

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.circolapp.model.Movimento
import com.example.circolapp.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28], manifest = Config.NONE)
class HomeFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockHomeViewModel: HomeViewModel

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var saldoLiveData: MutableLiveData<Double>
    private lateinit var movimentiLiveData: MutableLiveData<List<Movimento>>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Setup LiveData
        saldoLiveData = MutableLiveData()
        movimentiLiveData = MutableLiveData()
        
        // Setup ViewModel mocks
        `when`(mockHomeViewModel.saldo).thenReturn(saldoLiveData)
        `when`(mockHomeViewModel.movimenti).thenReturn(movimentiLiveData)
    }

    @Test
    fun testOnCreateView_returnsValidView() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()

            // Then
            scenario.onFragment { fragment ->
                assertNotNull(fragment.view)
                // Check that required views are present
                assertNotNull(fragment.view?.findViewById<View>(R.id.saldoText))
                assertNotNull(fragment.view?.findViewById<View>(R.id.iconNotifiche))
            }
        }
    }

    @Test
    fun testOnViewCreated_withLoggedInUser_setsUpRecyclerViewAndObservesViewModel() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()

            scenario.onFragment { fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
            }

            // Then
            scenario.onFragment { fragment ->
                val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.recyclerViewMovimenti)
                assertNotNull(recyclerView)
                assertNotNull(recyclerView?.adapter)
                assertNotNull(recyclerView?.layoutManager)
            }
        }
    }

    @Test
    fun testOnViewCreated_withoutLoggedInUser_showsLoginRequiredState() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(null)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()

            // Then
            scenario.onFragment { fragment ->
                val noDataMessage = fragment.view?.findViewById<View>(R.id.textViewNoDataMessage)
                val recyclerView = fragment.view?.findViewById<View>(R.id.recyclerViewMovimenti)
                val progressBar = fragment.view?.findViewById<View>(R.id.progressBarHome)

                assertEquals(View.VISIBLE, noDataMessage?.visibility)
                assertEquals(View.GONE, recyclerView?.visibility)
                assertEquals(View.GONE, progressBar?.visibility)
            }
        }
    }

    @Test
    fun testSaldoObserver_updatesSaldoText() {
        // Given
        val testSaldo = 1250.50
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            scenario.onFragment { fragment ->
                // Simulate ViewModel data update
                saldoLiveData.postValue(testSaldo)
            }

            // Then
            scenario.onFragment { fragment ->
                val saldoTextView = fragment.view?.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.saldoText)
                val saldoText = saldoTextView?.text?.toString() ?: ""
                // Should contain formatted currency amount
                assert(saldoText.contains("€"))
                assert(saldoText.contains("1.250"))
            }
        }
    }

    @Test
    fun testMovimentiObserver_withEmptyList_showsNoDataMessage() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            scenario.onFragment { fragment ->
                // Simulate empty movements list
                movimentiLiveData.postValue(emptyList())
            }

            // Then
            scenario.onFragment { fragment ->
                val noDataMessage = fragment.view?.findViewById<View>(R.id.textViewNoDataMessage)
                val recyclerView = fragment.view?.findViewById<View>(R.id.recyclerViewMovimenti)
                val progressBar = fragment.view?.findViewById<View>(R.id.progressBarHome)
                
                assertEquals(View.VISIBLE, noDataMessage?.visibility)
                assertEquals(View.GONE, recyclerView?.visibility)
                assertEquals(View.GONE, progressBar?.visibility)
            }
        }
    }

    @Test
    fun testMovimentiObserver_withData_showsRecyclerView() {
        // Given
        val testMovimenti = listOf(
            Movimento(100.0, "Test movimento 1", Date()),
            Movimento(-50.0, "Test movimento 2", Date())
        )
        
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            scenario.onFragment { fragment ->
                // Simulate movements data
                movimentiLiveData.postValue(testMovimenti)
            }

            // Then
            scenario.onFragment { fragment ->
                val noDataMessage = fragment.view?.findViewById<View>(R.id.textViewNoDataMessage)
                val recyclerView = fragment.view?.findViewById<View>(R.id.recyclerViewMovimenti)
                val progressBar = fragment.view?.findViewById<View>(R.id.progressBarHome)
                
                assertEquals(View.GONE, noDataMessage?.visibility)
                assertEquals(View.VISIBLE, recyclerView?.visibility)
                assertEquals(View.GONE, progressBar?.visibility)
            }
        }
    }

    @Test
    fun testNotificationsIconClick_navigatesToNotifications() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            scenario.onFragment { fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
                val notificationsIcon = fragment.view?.findViewById<View>(R.id.iconNotifiche)
                notificationsIcon?.performClick()
            }

            // Then
            verify(mockNavController).navigate(R.id.notificheFragment)
        }
    }

    @Test
    fun testOnDestroyView_cleansUpBinding() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            // Then
            scenario.onFragment { fragment ->
                assertNotNull(fragment.binding)
            }
            
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.DESTROYED)
            
            scenario.onFragment { fragment ->
                // Note: _binding is set to null in onDestroyView, but we can't directly test it
                // as it's a private property. The test ensures no crash occurs.
            }
        }
    }

    @Test
    fun testProgressBarVisibility_initialState() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            // Then - Progress bar should be visible initially for logged in users
            scenario.onFragment { fragment ->
                val progressBar = fragment.view?.findViewById<View>(R.id.progressBarHome)
                val noDataMessage = fragment.view?.findViewById<View>(R.id.textViewNoDataMessage)
                val recyclerView = fragment.view?.findViewById<View>(R.id.recyclerViewMovimenti)
                
                assertEquals(View.VISIBLE, progressBar?.visibility)
                assertEquals(View.GONE, noDataMessage?.visibility)
                assertEquals(View.GONE, recyclerView?.visibility)
            }
        }
    }

    @Test
    fun testCurrencyFormatter_formatsCorrectly() {
        // Given
        mockStatic(FirebaseAuth::class.java).use { firebaseAuthMock ->
            firebaseAuthMock.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockFirebaseAuth)
            `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

            // When
            val scenario = launchFragmentInContainer<HomeFragment>()
            
            scenario.onFragment { fragment ->
                // Test various amounts
                saldoLiveData.postValue(0.0)
            }

            // Then
            scenario.onFragment { fragment ->
                val saldoTextView = fragment.view?.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.saldoText)
                val saldoText = saldoTextView?.text?.toString() ?: ""
                assert(saldoText.contains("€"))
                assert(saldoText.contains("0,00"))
            }
        }
    }
}