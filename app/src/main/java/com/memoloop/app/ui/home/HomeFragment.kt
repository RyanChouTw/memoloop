package com.memoloop.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.memoloop.app.R
import com.memoloop.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.streak.observe(viewLifecycleOwner) { streak ->
            binding.tvStreakCount.text = streak.toString()
        }

        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_review)
        }

        // Long-press streak card → reset dialog (for testing)
        binding.tvStreak.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("重置複習紀錄")
                .setMessage("確定要清除所有複習紀錄與連續天數嗎？")
                .setPositiveButton("確定重置") { _, _ -> viewModel.resetData() }
                .setNegativeButton("取消", null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStreak()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
